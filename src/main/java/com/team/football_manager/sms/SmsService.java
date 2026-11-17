package com.team.football_manager.sms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SmsService {

    private static final Logger log = LoggerFactory.getLogger(SmsService.class);

    private final TwilioClient twilio;

    public SmsService(TwilioClient twilio) {
        this.twilio = twilio;
    }

    public boolean isEnabled() {
        return twilio.isConfigured();
    }

    /** Sends in the background so the admin doesn't wait for every message. */
    @Async
    public void sendAll(List<SmsMessage> messages) {
        if (messages.isEmpty()) {
            return;
        }

        if (!twilio.isConfigured()) {
            log.warn("SMS is not configured (TWILIO_* env vars). {} message(s) not sent. Example:\n{}",
                    messages.size(), messages.get(0).body());
            return;
        }

        int sent = 0;
        for (SmsMessage message : messages) {
            try {
                twilio.send(message.to(), message.body());
                sent++;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("SMS sending interrupted after {} message(s)", sent);
                return;
            } catch (Exception e) {
                log.warn("SMS to {} ({}) failed: {}", message.recipientName(), mask(message.to()), e.getMessage());
            }
        }
        log.info("SMS: sent {}/{} message(s)", sent, messages.size());
    }

    private static String mask(String phone) {
        return phone.length() <= 4 ? "****" : "****" + phone.substring(phone.length() - 4);
    }
}
