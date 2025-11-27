package com.sitka.satellite.controller;

import com.sitka.satellite.service.WatiMessageService;
import com.sitka.satellite.service.GoogleMapsService;
import java.io.File;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/wati")
@CrossOrigin(origins = "*")
public class WatiTestController {

    @Autowired
    private WatiMessageService watiMessageService;

    @Autowired
    private GoogleMapsService googleMapsService;

    /**
     * Endpoint de teste para enviar mensagem de texto simples
     * POST /wati/send-message
     * 
     * Body JSON:
     * {
     *   "phoneNumber": "5511989838304",
     *   "message": "Olá! Esta é uma mensagem de teste."
     * }
     */
    @PostMapping("/send-message")
    public ResponseEntity<Map<String, Object>> sendMessage(
            @RequestParam String phoneNumber,
            @RequestParam String message) {

        Map<String, Object> result = watiMessageService.sendTextMessage(phoneNumber, message);
        return ResponseEntity.ok(result);
    }

    /**
     * Endpoint de teste para enviar mensagem com JSON no body
     * POST /wati/send-message-json
     * 
     * Body JSON:
     * {
     *   "phoneNumber": "5511989838304",
     *   "message": "Olá! Esta é uma mensagem de teste."
     * }
     */
    @PostMapping("/send-message-json")
    public ResponseEntity<Map<String, Object>> sendMessageJson(
            @RequestBody Map<String, String> request) {

        String phoneNumber = request.get("phoneNumber");
        String message = request.get("message");

        if (phoneNumber == null || phoneNumber.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("ok", false);
            error.put("message", "phoneNumber é obrigatório");
            return ResponseEntity.badRequest().body(error);
        }

        if (message == null || message.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("ok", false);
            error.put("message", "message é obrigatório");
            return ResponseEntity.badRequest().body(error);
        }

        Map<String, Object> result = watiMessageService.sendTextMessage(phoneNumber, message);
        return ResponseEntity.ok(result);
    }

    /**
     * Endpoint de teste para enviar mensagem com template
     * POST /wati/send-template
     * 
     * Body JSON:
     * {
     *   "phoneNumber": "5511989838304",
     *   "templateName": "welcome",
     *   "parameters": {
     *     "name": "João",
     *     "date": "27/11/2025"
     *   }
     * }
     */
    @PostMapping("/send-template")
    public ResponseEntity<Map<String, Object>> sendTemplate(
            @RequestBody Map<String, Object> request) {

        String phoneNumber = (String) request.get("phoneNumber");
        String templateName = (String) request.get("templateName");
        @SuppressWarnings("unchecked")
        Map<String, String> parameters = (Map<String, String>) request.get("parameters");

        if (phoneNumber == null || phoneNumber.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("ok", false);
            error.put("message", "phoneNumber é obrigatório");
            return ResponseEntity.badRequest().body(error);
        }

        if (templateName == null || templateName.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("ok", false);
            error.put("message", "templateName é obrigatório");
            return ResponseEntity.badRequest().body(error);
        }

        Map<String, Object> result = watiMessageService.sendTemplateMessage(phoneNumber, templateName, parameters);
        return ResponseEntity.ok(result);
    }

    /**
     * Endpoint para enviar imagem de satélite
     * POST /wati/send-satellite-image
     * 
     * Body JSON:
     * {
     *   "phoneNumber": "5511989838304",
     *   "address": "Av Dr Guilherme Dumont Vilares 2000"
     * }
     */
    @PostMapping("/send-satellite-image")
    public ResponseEntity<Map<String, Object>> sendSatelliteImage(
            @RequestBody Map<String, String> request) {

        String phoneNumber = request.get("phoneNumber");
        String address = request.get("address");

        if (phoneNumber == null || phoneNumber.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("ok", false);
            error.put("message", "phoneNumber é obrigatório");
            return ResponseEntity.badRequest().body(error);
        }

        if (address == null || address.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("ok", false);
            error.put("message", "address é obrigatório");
            return ResponseEntity.badRequest().body(error);
        }

        // Gerar imagem de satélite
        File satelliteImage = googleMapsService.getSatelliteImage(address);

        if (satelliteImage == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("ok", false);
            error.put("message", "Erro ao gerar imagem de satélite");
            return ResponseEntity.status(500).body(error);
        }

        // Enviar imagem de satélite via WATI
        String caption = "Imagem de satélite para: " + address;
        Map<String, Object> result = watiMessageService.sendFile(phoneNumber, satelliteImage, caption);

        // Deletar imagem temporária
        satelliteImage.delete();
        return ResponseEntity.ok(result);
    }

    /**
     * Endpoint de informações sobre os endpoints WATI
     * GET /wati/info
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> info = new HashMap<>();
        info.put("ok", true);
        info.put("service", "WATI Message Service");
        info.put("endpoints", new HashMap<String, String>() {{
            put("POST /wati/send-message", "Enviar mensagem de texto (query params)");
            put("POST /wati/send-message-json", "Enviar mensagem de texto (JSON body)");
            put("POST /wati/send-template", "Enviar mensagem com template");
            put("POST /wati/send-satellite-image", "Enviar imagem de satélite");
            put("GET /wati/info", "Informações dos endpoints");
        }});
        info.put("example_request", new HashMap<String, Object>() {{
            put("method", "POST");
            put("url", "/wati/send-message-json");
            put("body", new HashMap<String, String>() {{
                put("phoneNumber", "5511989838304");
                put("message", "Olá! Esta é uma mensagem de teste.");
            }});
        }});
        return ResponseEntity.ok(info);
    }
}
