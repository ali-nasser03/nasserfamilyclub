package com.team.football_manager.service;

import com.team.football_manager.dto.RegisterRequest;
import com.team.football_manager.dto.UpdatePlayerRequest;
import com.team.football_manager.model.Role;
import com.team.football_manager.model.User;
import com.team.football_manager.repository.AttendanceRepository;
import com.team.football_manager.repository.AuthTokenRepository;
import com.team.football_manager.repository.PaymentRepository;
import com.team.football_manager.repository.UserRepository;
import com.team.football_manager.util.ApiException;
import com.team.football_manager.util.Names;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlayerService {

    private final UserRepository users;
    private final AttendanceRepository attendance;
    private final PaymentRepository payments;
    private final AuthTokenRepository tokens;

    public PlayerService(UserRepository users, AttendanceRepository attendance, PaymentRepository payments,
                         AuthTokenRepository tokens) {
        this.users = users;
        this.attendance = attendance;
        this.payments = payments;
        this.tokens = tokens;
    }

    @Transactional
    public User register(RegisterRequest request) {
        String name = Names.clean(request.fullName());
        ensureNameFree(name, null);

        User user = new User();
        user.setFullName(name);
        user.setAge(request.age());
        user.setWorking(request.working());
        user.setRole(Role.PLAYER);
        return users.save(user);
    }

    @Transactional
    public void update(Long id, UpdatePlayerRequest request) {
        User user = findPlayer(id);
        String name = Names.clean(request.fullName());
        ensureNameFree(name, id);

        user.setFullName(name);
        user.setAge(request.age());
        user.setWorking(request.working());
    }

    @Transactional
    public void delete(Long id) {
        User user = findPlayer(id);
        attendance.deleteByPlayer_Id(id);
        payments.deleteByPlayer_Id(id);
        tokens.deleteByUser_Id(id);
        users.delete(user);
    }

    public User findPlayer(Long id) {
        User user = users.findById(id).orElseThrow(() -> ApiException.notFound("اللاعب غير موجود"));
        if (user.isAdmin()) {
            throw ApiException.badRequest("لا يمكن تعديل حساب الإدارة من هنا");
        }
        return user;
    }

    private void ensureNameFree(String name, Long ownId) {
        if (name.isEmpty()) {
            throw ApiException.badRequest("الاسم مطلوب");
        }
        users.findByFullName(name).ifPresent(existing -> {
            if (!existing.getId().equals(ownId)) {
                throw ApiException.badRequest("هذا الاسم مسجل مسبقاً");
            }
        });
    }
}
