package com.team.football_manager.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Admin edit of a player. */
public record UpdatePlayerRequest(
        @NotBlank(message = "الاسم مطلوب")
        @Size(max = 80, message = "الاسم طويل كثير")
        String fullName,


        @Min(value = 5, message = "العمر غير صحيح")
        @Max(value = 99, message = "العمر غير صحيح")
        int age,

        boolean working
) {
}
