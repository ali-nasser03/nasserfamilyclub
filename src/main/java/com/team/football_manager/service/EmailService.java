package com.team.football_manager.service;

import com.team.football_manager.model.Match;
import com.team.football_manager.model.User;
import com.team.football_manager.repository.UserRepository;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class EmailService {

    @Autowired
    private UserRepository userRepository;

    @Value("${RESEND_API_KEY}")
    private String resendApiKey;

    private final OkHttpClient client = new OkHttpClient();

    public void notifyPlayersAboutMatch(Match match) {

        List<User> players = userRepository.findAll()
                .stream()
                .filter(user -> !"ADMIN".equalsIgnoreCase(user.getRole()))
                .filter(user -> user.getEmail() != null && !user.getEmail().isBlank())
                .toList();

        for (User player : players) {
            try {
                sendEmail(player, match);
            } catch (Exception e) {
                System.out.println("Failed to send email to "
                        + player.getFullName() + ": "
                        + e.getMessage());
            }
        }
    }

    private void sendEmail(User player, Match match) throws IOException {

        String note = match.getNote() == null
                ? ""
                : match.getNote();

        String body = """
                مرحباً %s

                تم نشر مباراة جديدة ⚽

                المكان: %s

                الموعد: %s

                الملاحظة:
                %s

                ادخل للموقع وقم بالتصويت.
                """
                .formatted(
                        player.getFullName(),
                        match.getLocation(),
                        match.getDateTime(),
                        note
                );

        String json = """
                {
                  "from":"Football Team <onboarding@resend.dev>",
                  "to":["%s"],
                  "subject":"⚽ مباراة جديدة",
                  "text":"%s"
                }
                """
                .formatted(
                        player.getEmail(),
                        body.replace("\"","'")
                );

        Request request = new Request.Builder()
                .url("https://api.resend.com/emails")
                .addHeader("Authorization", "Bearer " + resendApiKey)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(
                        json,
                        MediaType.parse("application/json")
                ))
                .build();

        Response response = client.newCall(request).execute();

        System.out.println(response.code());
    }
}
