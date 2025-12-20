package com.game.tree;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GameApp {
    public static void main(String[] args) {
        System.out.println("ДЕРЕВО-КЛИКЕР");
        System.out.println("======================================");
        SpringApplication.run(GameApp.class, args);
        System.out.println("SPRING BOOT ЗАПУЩЕН УСПЕШНО!");
        System.out.println("в браузере: http://localhost:8080");
        System.out.println("API статус: http://localhost:8080/status");
    }
}