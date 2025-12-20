package com.game.tree;

import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
public class GameController {

    private int coins = 50;
    private int clicks = 0;
    private String tree = "🌱";

    @GetMapping("/")
    public String home() {
        String html = "<!DOCTYPE html>" +
                     "<html>" +
                     "<head>" +
                     "    <title>Игра 'Расти Дерево'</title>" +
                     "    <style>" +
                     "        body { font-family: Arial; padding: 20px; text-align: center; }" +
                     "        .tree { font-size: 100px; margin: 20px; }" +
                     "        button { padding: 15px 30px; font-size: 18px; margin: 10px; }" +
                     "        .stats { background: #f0f0f0; padding: 15px; border-radius: 10px; display: inline-block; }" +
                     "    </style>" +
                     "</head>" +
                     "<body>" +
                     "    <h1>🌳 Игра 'Расти Дерево' 🌳</h1>" +
                     "    <div class='stats'>" +
                     "        <p>Монеты: <span id='coins'>" + coins + "</span> 🪙</p>" +
                     "        <p>Кликов: <span id='clicks'>" + clicks + "</span></p>" +
                     "    </div>" +
                     "    <div class='tree' id='tree'>" + tree + "</div>" +
                     "    <div>" +
                     "        <button onclick='water()'>💧 Полить дерево</button>" +
                     "        <button onclick='buy()'> Купить удобрение (20 монет)</button>" +
                     "    </div>" +
                     "    <p id='message'></p>" +
                     "    <script>" +
                     "        async function water() {" +
                     "            const response = await fetch('/click', { method: 'POST' });" +
                     "            const data = await response.json();" +
                     "            updateGame(data);" +
                     "        }" +
                     "        async function buy() {" +
                     "            const response = await fetch('/buy', { method: 'POST' });" +
                     "            const data = await response.json();" +
                     "            updateGame(data);" +
                     "        }" +
                     "        function updateGame(data) {" +
                     "            document.getElementById('coins').textContent = data.coins;" +
                     "            document.getElementById('clicks').textContent = data.clicks;" +
                     "            document.getElementById('tree').textContent = data.tree;" +
                     "            document.getElementById('message').textContent = data.message;" +
                     "        }" +
                     "    </script>" +
                     "</body>" +
                     "</html>";
        return html;
    }

    @GetMapping("/status")
    public Map<String, Object> status() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "ok");
        response.put("coins", coins);
        response.put("clicks", clicks);
        response.put("tree", tree);
        response.put("message", "Игра работает!");
        return response;
    }

    @PostMapping("/click")
    public Map<String, Object> click() {
        coins++;
        clicks++;
        updateTree();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("coins", coins);
        response.put("clicks", clicks);
        response.put("tree", tree);
        response.put("message", "Дерево полито! +1 монета");
        
        return response;
    }

    @PostMapping("/buy")
    public Map<String, Object> buy() {
        Map<String, Object> response = new HashMap<>();
        
        if (coins >= 20) {
            coins -= 20;
            clicks += 10;
            updateTree();
            
            response.put("success", true);
            response.put("coins", coins);
            response.put("clicks", clicks);
            response.put("tree", tree);
            response.put("message", "Удобрение куплено! +10 кликов");
        } else {
            response.put("success", false);
            response.put("message", "Недостаточно монет!");
        }
        
        return response;
    }

    @GetMapping("/test")
    public String test() {
        return "Сервер работает " + System.currentTimeMillis();
    }

    private void updateTree() {
        if (clicks >= 100) tree = "🎄";
        else if (clicks >= 60) tree = "🌲";
        else if (clicks >= 30) tree = "🌳";
        else if (clicks >= 10) tree = "🌿";
        else tree = "🌱";
    }
}