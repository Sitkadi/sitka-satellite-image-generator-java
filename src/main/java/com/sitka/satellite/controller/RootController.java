package com.sitka.satellite.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
public class RootController {

    /**
     * Endpoint raiz - Retorna boas-vindas
     * GET /
     */
    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> root() {
        Map<String, Object> response = new HashMap<>();
        response.put("ok", true);
        response.put("message", "Bem-vindo ao Sitka Satellite Image Generator!");
        response.put("service", "Sitka Satellite Image Generator");
        response.put("version", "1.0.0");
        response.put("status", "UP");
        response.put("endpoints", new HashMap<String, String>() {{
            put("POST /analise-imagemdesatelite", "Gerar e enviar imagem de satélite via WATI");
            put("GET /analise-imagemdesatelite/health", "Health check");
            put("GET /", "Informações da aplicação");
            put("GET /health", "Status da aplicação");
            put("GET /status", "Status detalhado");
        }});
        return ResponseEntity.ok(response);
    }

    /**
     * Health check endpoint na raiz
     * GET /health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Sitka Satellite Image Generator");
        response.put("timestamp", String.valueOf(System.currentTimeMillis()));
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint de status detalhado
     * GET /status
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        Map<String, Object> response = new HashMap<>();
        response.put("ok", true);
        response.put("service", "Sitka Satellite Image Generator");
        response.put("version", "1.0.0");
        response.put("status", "UP");
        response.put("timestamp", System.currentTimeMillis());
        response.put("uptime", Runtime.getRuntime().totalMemory());
        response.put("java_version", System.getProperty("java.version"));
        response.put("os_name", System.getProperty("os.name"));
        response.put("description", "API para gerar imagens de satélite e enviar via WATI");
        return ResponseEntity.ok(response);
    }

}
