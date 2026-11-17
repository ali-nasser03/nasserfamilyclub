package com.team.football_manager.repository;

import com.team.football_manager.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByMonth(String month);

    Optional<Payment> findByPlayer_IdAndMonth(Long playerId, String month);

    void deleteByPlayer_Id(Long playerId);
}
