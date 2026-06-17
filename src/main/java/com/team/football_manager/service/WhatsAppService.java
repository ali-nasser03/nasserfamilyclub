package com.team.football_manager.service;

import com.team.football_manager.model.Match;
import com.team.football_manager.model.User;
import com.team.football_manager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@Service
public class WhatsAppService {

    @Autowired
    private UserRepository userRepository;

    @Value("${whatsapp.api-url:https://graph.facebook.com/v20.0}")
    private String apiUrl;

    @Value("${whatsapp.phone-number-id:}")
    private String phoneNumberId;

    @Value("${whatsapp.access-token:}")
    private String accessToken;

    @Value("${whatsapp.template-name:match_notification}")
    private String templateName;

    @Value("${whatsapp.language-code:ar}")
    private String languageCode;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public void notifyPlayersAboutMatch(Match match) {
        if (!isConfigured()) {
            System.out.println("WhatsApp is not configured. Skipping notifications.");
            return;
        }

        List<User> players = userRepository.findAll()
                .stream()
                .filter(user -> !"ADMIN".equalsIgnoreCase(user.getRole()))
                .filter(user -> user.getPhoneNumber() != null && !user.getPhoneNumber().trim().isEmpty())
                .toList();

        for (User player : players) {
            try {
                sendMatchTemplate(player.getPhoneNumber(), match);
            } catch (Exception e) {
                System.out.println("Failed to send WhatsApp to " + player.getFullName() + ": " + e.getMessage());
            }
        }
    }

    private boolean isConfigured() {
        return phoneNumberId != null && !phoneNumberId.isBlank()
                && accessToken != null && !accessToken.isBlank();
    }

    private void sendMatchTemplate(String rawPhoneNumber, Match match) throws Exception {
        String phoneNumber = normalizePhone(rawPhoneNumber);

        String location = match.getLocation() == null ? "-" : match.getLocation();
        String dateTime = match.getDateTime() == null ? "-" : match.getDateTime().toString().replace("T", " ");
        String note = match.getNote() == null || match.getNote().isBlank() ? "لا توجد ملاحظات" : match.getNote();

        String json = """
                {
                  "messaging_product": "whatsapp",
                  "to": "%s",
                  "type": "template",
                  "template": {
                    "name": "%s",
                    "language": {
                      "code": "%s"
                    },
                    "components": [
                      {
                        "type": "body",
                        "parameters": [
                          { "type": "text", "text": "%s" },
                          { "type": "text", "text": "%s" },
                          { "type": "text", "text": "%s" }
                        ]
                      }
                    ]
                  }
                }
                """.formatted(
                escapeJson(phoneNumber),
                escapeJson(templateName),
                escapeJson(languageCode),
                escapeJson(location),
                escapeJson(dateTime),
                escapeJson(note)
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl + "/" + phoneNumberId + "/messages"))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("WhatsApp API error: " + response.statusCode() + " - " + response.body());
        }
    }

    private String normalizePhone(String phone) {
        String cleaned = phone.replace("+", "").replace(" ", "").replace("-", "").trim();

        if (cleaned.startsWith("00")) {
            cleaned = cleaned.substring(2);
        }

        return cleaned;
    }

    private String escapeJson(String value) {
        if (value == null) return "";
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", " ")
                .replace("\r", " ");
    }
}
