package com.team.football_manager.service;

import com.team.football_manager.dto.PaymentStatus;
import com.team.football_manager.model.Payment;
import com.team.football_manager.model.User;
import com.team.football_manager.repository.PaymentRepository;
import com.team.football_manager.util.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

/** Monthly fee. A new month starts with everyone unpaid, so there is no "reset" button anymore. */
@Service
public class PaymentService {

    private final PaymentRepository payments;
    private final PlayerService players;

    public PaymentService(PaymentRepository payments, PlayerService players) {
        this.payments = payments;
        this.players = players;
    }

    /** Ids of players who paid in the given month. */
    @Transactional(readOnly = true)
    public Set<Long> paidPlayerIds(String month) {
        return payments.findByMonth(month).stream()
                .map(p -> p.getPlayer().getId())
                .collect(Collectors.toSet());
    }

    public static PaymentStatus statusOf(User user, Set<Long> paidIds) {
        if (user.isExemptFromPayment()) {
            return PaymentStatus.EXEMPT;
        }
        return paidIds.contains(user.getId()) ? PaymentStatus.PAID : PaymentStatus.UNPAID;
    }

    @Transactional
    public void setPaid(Long playerId, String month, boolean paid) {
        User player = players.findPlayer(playerId);
        if (player.isExemptFromPayment()) {
            throw ApiException.badRequest("هذا اللاعب معفي من الدفع");
        }

        var existing = payments.findByPlayer_IdAndMonth(playerId, month);
        if (paid && existing.isEmpty()) {
            payments.save(new Payment(player, month));
        } else if (!paid) {
            existing.ifPresent(payments::delete);
        }
    }
}
