package com.team.football_manager.service;

import com.team.football_manager.dto.*;
import com.team.football_manager.model.Attendance;
import com.team.football_manager.model.Match;
import com.team.football_manager.model.User;
import com.team.football_manager.model.Vote;
import com.team.football_manager.repository.AttendanceRepository;
import com.team.football_manager.repository.MatchRepository;
import com.team.football_manager.repository.UserRepository;
import com.team.football_manager.util.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MatchService {

    private static final Logger log = LoggerFactory.getLogger(MatchService.class);

    private final MatchRepository matches;
    private final AttendanceRepository attendance;
    private final UserRepository users;
    private final PaymentService payments;
    private final Clock clock;

    public MatchService(MatchRepository matches, AttendanceRepository attendance, UserRepository users,
                        PaymentService payments, Clock clock) {
        this.matches = matches;
        this.attendance = attendance;
        this.users = users;
        this.payments = payments;
        this.clock = clock;
    }

    public Optional<Match> currentMatch() {
        return matches.findFirstByActiveTrueOrderByDateTimeDesc();
    }

    // ---------------------------------------------------------------- admin

    @Transactional
    public MatchDto create(CreateMatchRequest request) {
        if (!request.dateTime().isAfter(clock.now())) {
            throw ApiException.badRequest("موعد المباراة لازم يكون بالمستقبل");
        }

        // Only one open match at a time. Old matches keep their votes as history.
        List<Match> open = matches.findByActiveTrue();
        open.forEach(m -> m.setActive(false));

        Match match = new Match();
        match.setLocation(request.location().trim());
        match.setDateTime(request.dateTime());
        match.setNote(request.note() == null || request.note().isBlank() ? null : request.note().trim());
        match.setActive(true);
        matches.save(match);

        return MatchDto.from(match);
    }

    @Transactional
    public void closeCurrent() {
        matches.findByActiveTrue().forEach(m -> m.setActive(false));
    }

    @Transactional(readOnly = true)
    public AdminOverview adminOverview(String month) {
        Match match = currentMatch().orElse(null);
        Map<Long, Vote> votes = votesOf(match);
        Set<Long> paid = payments.paidPlayerIds(month);

        List<AdminOverview.Row> rows = users.findPlayers().stream()
                .map(u -> new AdminOverview.Row(u.getId(), u.getFullName(), u.getAge(), u.isWorking(),
                        votes.get(u.getId()), PaymentService.statusOf(u, paid)))
                .toList();

        return new AdminOverview(MatchDto.from(match), month, rows);
    }

    // ---------------------------------------------------------------- players

    @Transactional(readOnly = true)
    public BoardResponse board(User me, String month) {
        Match match = currentMatch().orElse(null);
        Map<Long, Vote> votes = votesOf(match);

        List<BoardResponse.Entry> entries = users.findPlayers().stream()
                .map(u -> new BoardResponse.Entry(u.getId(), u.getFullName(), votes.get(u.getId())))
                .toList();

        Set<Long> paid = payments.paidPlayerIds(month);
        BoardResponse.Me myStatus = new BoardResponse.Me(me.getId(), me.getFullName(),
                votes.get(me.getId()), PaymentService.statusOf(me, paid));

        return new BoardResponse(MatchDto.from(match), month, entries, myStatus);
    }

    @Transactional
    public void vote(User player, Vote vote) {
        Match match = currentMatch().orElseThrow(() -> ApiException.badRequest("التصويت مسكّر، ما في مباراة منشورة"));

        Attendance row = attendance.findByMatch_IdAndPlayer_Id(match.getId(), player.getId())
                .orElseGet(() -> new Attendance(users.getReferenceById(player.getId()), match, vote));
        row.setStatus(vote);
        attendance.save(row);
    }

    @Transactional
    public void clearVote(User player) {
        currentMatch().flatMap(m -> attendance.findByMatch_IdAndPlayer_Id(m.getId(), player.getId()))
                .ifPresent(attendance::delete);
    }

    private Map<Long, Vote> votesOf(Match match) {
        if (match == null) {
            return Map.of();
        }
        return attendance.findByMatch_Id(match.getId()).stream()
                .filter(a -> a.getStatus() != null)
                .collect(Collectors.toMap(a -> a.getPlayer().getId(), Attendance::getStatus, (a, b) -> b));
    }

    // ---------------------------------------------------------------- scheduler

    /** Voting closes automatically when the match starts. Votes are kept as history. */
    @Scheduled(fixedDelay = 60_000, initialDelay = 30_000)
    @Transactional
    public void closeStartedMatches() {
        LocalDateTime now = clock.now();
        List<Match> started = matches.findByActiveTrueAndDateTimeLessThanEqual(now);
        for (Match match : started) {
            match.setActive(false);
            log.info("Voting closed for match {} ({} at {})", match.getId(), match.getLocation(), match.getDateTime());
        }
    }
}
