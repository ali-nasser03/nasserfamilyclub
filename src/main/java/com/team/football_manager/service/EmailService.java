package com.team.football_manager.service;

import com.team.football_manager.model.Match;
import com.team.football_manager.model.User;
import com.team.football_manager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private UserRepository userRepository;

    public void notifyPlayersAboutMatch(Match match) {
        List<User> players = userRepository.findAll()
                .stream()
                .filter(user -> !"ADMIN".equalsIgnoreCase(user.getRole()))
                .filter(user -> user.getEmail() != null && !user.getEmail().isBlank())
                .toList();

        for (User player : players) {
            try {
                sendMatchEmail(player, match);
            } catch (Exception e) {
                System.out.println("Failed to send email to " + player.getFullName() + ": " + e.getMessage());
            }
        }
    }

    private void sendMatchEmail(User player, Match match) {
        String dateTime = match.getDateTime() == null
                ? "-"
                : match.getDateTime().toString().replace("T", " ");

        String note = match.getNote() == null || match.getNote().isBlank()
                ? "لا توجد ملاحظات"
                : match.getNote();

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(player.getEmail());
        message.setSubject("⚽ مباراة جديدة");
        message.setText(
                "مرحبا " + player.getFullName() + "\n\n" +
                "تم نشر مباراة جديدة ⚽\n\n" +
                "📍 المكان: " + match.getLocation() + "\n" +
                "⏰ الوقت: " + dateTime + "\n" +
                "📝 ملاحظة: " + note + "\n\n" +
                "ادخل على الموقع وصوّت بالحضور أو الاعتذار."
        );

        mailSender.send(message);
    }
}
