# نادي عائلة ناصر

موقع لمباريات العيلة: الإدارة بتنشر مباراة، واللاعبين بتوصلهم رسالة SMS وبصوّتوا "سأحضر" أو "معتذر". الإدارة كمان بتتابع الاشتراك الشهري.

Spring Boot 4 + PostgreSQL، والواجهة HTML و JavaScript عادي بدون frameworks.

## متغيرات البيئة (Environment variables)

| المتغير                                                     | ضروري؟      | شو هو                                                                   |
|-------------------------------------------------------------|-------------|-------------------------------------------------------------------------|
| `SPRING_DATASOURCE_URL`                                     | نعم         | رابط قاعدة البيانات، مثل `jdbc:postgresql://host:5432/db`               |
| `SPRING_DATASOURCE_USERNAME` / `SPRING_DATASOURCE_PASSWORD` | حسب القاعدة |                                                                         |
| `APP_URL`                                                   | مستحسن      | رابط الموقع، بينحط بالرسائل (مثلاً `https://nasserfc.onrender.com`)     |
| `TWILIO_ACCOUNT_SID`                                        | للـ SMS     | من لوحة Twilio                                                          |
| `TWILIO_AUTH_TOKEN`                                         | للـ SMS     | من لوحة Twilio                                                          |
| `TWILIO_FROM`                                               | للـ SMS     | رقم Twilio (`+1...`) أو اسم مرسل مثل `NasserFC`                         |
| `TWILIO_MESSAGING_SERVICE_SID`                              | اختياري     | بدل `TWILIO_FROM` إذا بتستخدم Messaging Service                         |
| `ADMIN_PASSWORD`                                            | أول مرة بس  | إذا ما في حساب إدارة، بينعمل حساب باسم `ADMIN_NAME` (الافتراضي `Admin`) |
| `COOKIE_SECURE`                                             | مستحسن      | خليها `true` لما يكون الموقع على https                                  |

إذا إعدادات Twilio فاضية، الموقع بيشتغل عادي والرسائل بتنكتب بالـ log بس، ولوحة الإدارة بتنبّهك.

## ملاحظات Twilio

- بالحساب التجريبي (Trial) الرسائل بتوصل بس للأرقام اللي وثّقتها بـ Twilio.
- لازم تفعّل الإرسال لفلسطين (+970) و/أو إسرائيل (+972) من **Messaging → Settings → Geo permissions**.
- الرسالة العربية بتنحسب كل 70 حرف كرسالة. رسالة المباراة الجديدة تقريباً رسالتين، والتذكير رسالة وحدة.

## التشغيل محلياً

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/nasser
export SPRING_DATASOURCE_USERNAME=postgres SPRING_DATASOURCE_PASSWORD=postgres
export ADMIN_PASSWORD=change-me
mvn spring-boot:run
```

بعدها افتح http://localhost:8080

## الترقية من النسخة القديمة

ما في داعي تعمل إشي يدوي. عند أول تشغيل، `StartupTasks` بترتّب قاعدة البيانات الموجودة:
- كلمة سر الإدارة القديمة بتضل شغالة، وأول ما تدخل فيها بتتخزّن مشفّرة (BCrypt).
- علامات "دافع" القديمة بتنتقل لجدول `payments` للشهر الحالي.
- التصويتات الفاضية والمكررة بتنمسح.
- اللاعبين القدام ما إلهم رقم جوال. الإدارة بتضيف أرقامهم من تبويب "اللاعبين".
