package com.team.football_manager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record CreateMatchRequest(
        @NotBlank(message = "المكان مطلوب")
        @Size(max = 200, message = "اسم المكان طويل كثير")
        String location,

        @NotNull(message = "الموعد مطلوب")
        LocalDateTime dateTime,

        @Size(max = 1000, message = "الملاحظة طويلة كثير")
        String note
) {
}
