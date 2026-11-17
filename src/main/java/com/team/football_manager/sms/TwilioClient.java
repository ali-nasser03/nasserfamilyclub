package com.team.football_manager.sms;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Minimal Twilio client using Java's built-in HttpClient (no extra library needed).
 * Docs: https://www.twilio.com/docs/messaging/api/message-resource#create-a-message-resource
 */
@Component
public class TwilioClient {

    private final TwilioProperties props;
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public TwilioClient(TwilioProperties props) {
        this.props = props;
    }

    public boolean isConfigured() {
        return props.isConfigured();
    }

    public void send(String to, String body) throws IOException, InterruptedException {
        Map<String, String> form = new LinkedHashMap<>();
        form.put("To", to);
        form.put("Body", body);
        if (TwilioProperties.notBlank(props.messagingServiceSid())) {
            form.put("MessagingServiceSid", props.messagingServiceSid().trim());
        } else {
            form.put("From", props.from().trim());
        }

        String encodedForm = form.entrySet().stream()
                .map(e -> enc(e.getKey()) + "=" + enc(e.getValue()))
                .collect(Collectors.joining("&"));

        String credentials = props.accountSid().trim() + ":" + props.authToken().trim();
        String auth = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.twilio.com/2010-04-01/Accounts/" + props.accountSid().trim() + "/Messages.json"))
                .timeout(Duration.ofSeconds(20))
                .header("Authorization", "Basic " + auth)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(encodedForm))
                .build();

        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() / 100 != 2) {
            String responseBody = response.body() == null ? "" : response.body();
            if (responseBody.length() > 300) {
                responseBody = responseBody.substring(0, 300);
            }
            throw new IOException("Twilio returned " + response.statusCode() + ": " + responseBody);
        }
    }

    private static String enc(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }
}
