// Shared helpers for all pages.

/** Escape text before putting it in HTML (protects against names like <script>). */
function esc(value) {
    return String(value ?? '')
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}

class ApiError extends Error {
    constructor(status, message, code) {
        super(message);
        this.status = status;
        this.code = code;
    }
}

// ---------------------------------------------------------------- sign-in key
// The server gives a key at login; we keep it on the phone and send it with every request.
// (No cookies: Safari on iPhone didn't keep the sign-in cookie.)
const TOKEN_KEY = 'nfcAuthToken';
let memoryToken = null;

// Safari with "Block All Cookies" also blocks localStorage. Then the key is handed to the next
// page in the address (#t=...), kept in memory, and removed from the address right away.
const storageWorks = (() => {
    try {
        localStorage.setItem('nfcTest', '1');
        localStorage.removeItem('nfcTest');
        return true;
    } catch {
        return false;
    }
})();

(() => {
    const m = location.hash.match(/^#t=([0-9a-f]{64})$/);
    if (m) {
        memoryToken = m[1];
        history.replaceState(null, '', location.pathname + location.search);
    }
})();

/** Go to another page of the site without losing the sign-in, even when storage is blocked. */
function goTo(page) {
    const token = getToken();
    location.replace(!storageWorks && token ? `${page}#t=${token}` : page);
}

function getToken() {
    return storageGet(TOKEN_KEY) || memoryToken;
}

function setToken(token) {
    memoryToken = token || null;
    if (token) {
        storageSet(TOKEN_KEY, token);
    } else {
        try { localStorage.removeItem(TOKEN_KEY); } catch { /* ignore */ }
    }
}

/**
 * Calls the API. Throws ApiError with the server's Arabic message.
 * On 401 (signed out) it sends the user back to the login page, unless `allow401` is set.
 */
async function api(method, path, body, { allow401 = false } = {}) {
    let res;
    try {
        res = await fetch(path, {
            method,
            cache: 'no-store',
            headers: {
                ...(body !== undefined ? { 'Content-Type': 'application/json' } : {}),
                ...(getToken() ? { 'X-Auth-Token': getToken() } : {}),
            },
            body: body !== undefined ? JSON.stringify(body) : undefined,
        });
    } catch (e) {
        throw new ApiError(0, 'ما في اتصال بالإنترنت');
    }

    let data = null;
    const text = await res.text();
    if (text) {
        try { data = JSON.parse(text); } catch { data = null; }
    }

    if (!res.ok) {
        if (res.status === 401 && !allow401) {
            setToken(null);
            // "?out" tells the login page not to redirect again, so it can never loop
            location.replace('login.html?out');
        }
        throw new ApiError(res.status, data?.error || 'صار خطأ، جرّب مرة ثانية', data?.code);
    }
    return data;
}

let toastTimer;
function toast(message, type = '') {
    let el = document.getElementById('toast');
    if (!el) {
        el = document.createElement('div');
        el.id = 'toast';
        el.className = 'toast';
        el.setAttribute('role', 'status');
        el.setAttribute('aria-live', 'polite');
        document.body.appendChild(el);
    }
    el.textContent = message;
    el.className = 'toast show ' + type;
    clearTimeout(toastTimer);
    toastTimer = setTimeout(() => el.classList.remove('show'), 3200);
}

/** Confirmation dialog. Resolves true if the user confirms. */
function confirmBox(title, message, okLabel = 'تأكيد', danger = false) {
    return new Promise(resolve => {
        const dlg = document.createElement('dialog');
        dlg.innerHTML = `
            <h3>${esc(title)}</h3>
            <p>${esc(message)}</p>
            <div class="actions">
                <button class="btn ${danger ? 'danger' : ''}" value="ok">${esc(okLabel)}</button>
                <button class="btn ghost" value="cancel">رجوع</button>
            </div>`;
        document.body.appendChild(dlg);
        dlg.addEventListener('click', e => {
            const btn = e.target.closest('button');
            if (btn) dlg.close(btn.value);
        });
        dlg.addEventListener('close', () => {
            resolve(dlg.returnValue === 'ok');
            dlg.remove();
        });
        dlg.showModal();
    });
}

const AR = 'ar-EG-u-nu-latn'; // month names like نوفمبر، ديسمبر, Western digits

function parseLocal(iso) {
    // "2026-11-05T19:30:00" is local club time; parse without time-zone shifts
    const [d, t = '00:00'] = String(iso).split('T');
    const [y, m, day] = d.split('-').map(Number);
    const [hh, mm] = t.split(':').map(Number);
    return new Date(y, m - 1, day, hh, mm);
}

function matchDay(iso) {
    const d = parseLocal(iso);
    const weekday = new Intl.DateTimeFormat(AR, { weekday: 'long' }).format(d);
    return `${weekday} ${d.getDate()}/${d.getMonth() + 1}`;
}

function matchTime(iso) {
    const d = parseLocal(iso);
    return `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
}

function monthName(yyyyMm) {
    const [y, m] = yyyyMm.split('-').map(Number);
    return new Intl.DateTimeFormat(AR, { month: 'long', year: 'numeric' }).format(new Date(y, m - 1, 1));
}

/** The match card (shield). `match` may be null. */
function shieldHtml(match, emptyText) {
    if (!match) {
        return `
            <div class="shield empty"><div class="shield-in">
                <p class="day">ما في مباراة هلأ</p>
                <p>${esc(emptyText)}</p>
            </div></div>`;
    }
    return `
        <div class="shield"><div class="shield-in">
            <p class="day">${esc(matchDay(match.dateTime))}</p>
            <p class="time">الساعة ${esc(matchTime(match.dateTime))}</p>
            <p class="place">${esc(match.location)}</p>
            ${match.note ? `<p class="note">${esc(match.note)}</p>` : ''}
        </div></div>`;
}

const VOTE_LABEL = { YES: 'سأحضر', NO: 'معتذر' };
const PAYMENT_LABEL = { PAID: 'دافع', UNPAID: 'مش دافع', EXEMPT: 'معفي' };

function byName(a, b) {
    return String(a.name).localeCompare(String(b.name), 'ar');
}

function groupVotes(players) {
    return {
        yes: players.filter(p => p.vote === 'YES').sort(byName),
        no: players.filter(p => p.vote === 'NO').sort(byName),
        wait: players.filter(p => !p.vote).sort(byName),
    };
}

function tallyHtml(g) {
    return `
        <div class="tally">
            <div class="t-yes"><b>${g.yes.length}</b><span>جايين</span></div>
            <div class="t-no"><b>${g.no.length}</b><span>معتذرين</span></div>
            <div class="t-wait"><b>${g.wait.length}</b><span>ما صوّتوا</span></div>
        </div>`;
}

function voteListHtml(g, meId) {
    const section = (title, list, pillClass, pillText) => list.length ? `
        <div class="group-title"><span>${title}</span><span class="muted">${list.length}</span></div>
        <ul class="list">
            ${list.map(p => `
                <li>
                    <span class="name ${p.id === meId ? 'me' : ''}">${esc(p.name)}${p.id === meId ? ' (أنت)' : ''}</span>
                    <span class="pill ${pillClass}">${pillText}</span>
                </li>`).join('')}
        </ul>` : '';

    return section('جايين', g.yes, 'yes', 'سأحضر')
        + section('معتذرين', g.no, 'no', 'معتذر')
        + section('لسا ما صوّتوا', g.wait, 'wait', 'ما صوّت');
}

async function logout() {
    try { await api('POST', '/api/auth/logout', undefined, { allow401: true }); } catch { /* ignore */ }
    setToken(null);
    location.replace('login.html?out');
}

function storageGet(key) {
    try { return localStorage.getItem(key); } catch { return null; }
}

function storageSet(key, value) {
    try { localStorage.setItem(key, value); } catch { /* private mode */ }
}
