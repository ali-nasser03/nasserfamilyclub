package com.team.football_manager.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AppController {

    // عند كتابة الرابط الأساسي، سيقوم المتصفح بفتح صفحة اللاعب تلقائياً
    @GetMapping("/")
    public String index() {
        return "forward:/player.html";
    }
}