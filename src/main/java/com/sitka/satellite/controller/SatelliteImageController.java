package com.sitka.satellite.controller;

import com.google.gson.JsonObject;
import com.sitka.satellite.service.SatelliteImageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/analise-imagemdesatelite")
@CrossOrigin(origins = "*")
public class SatelliteImageController {

    private static final Logger logger = LoggerFactory.getLogger(SatelliteImageController.class);

    @Autowired
    private SatelliteImageService satelliteImageService;

    /**
     * Endpoint para gerar e enviar imagem de satélite via WATI
     * 
     * POST /analise-imagemdesatelite
     * 
     * Body:
     * {
     *   "telefone": "5511976169677",
     *   "endereco": "Av. Dr. Guilherme Dumont Vilares, 2000, São Paulo, SP"
     * }
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> analisarImagemDesatelite(
            @RequestBody Map<String, String> request) {

        Map<String, Object> response = new HashMap<>();

        try {
            String telefone = request.get("telefone");
            String endereco = request.get("endereco");

            logger.info("========================================================");
            logger.info("NOVA REQUISIÇÃO");
            logger.info("Telefone: " + telefone);
            logger.info("Endereço: " + endereco);
            logger.info("========================================================");

            // Validações
            if (telefone == null || telefone.isEmpty()) {
                response.put("ok", false);
                response.put("result", "error");
                response.put("mensagem_imagemdesatelite", "Telefone não fornecido");
                return ResponseEntity.badRequest().body(response);
            }

            if (endereco == null || endereco.isEmpty()) {
                response.put("ok", false);
                response.put("result", "error");
                response.put("mensagem_imagemdesatelite", "Endereço não fornecido");
                return ResponseEntity.badRequest().body(response);
            }

            // 1. Gerar imagem de satélite
            logger.info("Gerando imagem para: " + endereco);
            BufferedImage image = satelliteImageService.generateSatelliteImage(endereco);

            if (image == null) {
                response.put("ok", false);
                response.put("result", "error");
                response.put("mensagem_imagemdesatelite", "Erro ao gerar imagem de satélite");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }

            // 2. Salvar imagem
            String imagePath = satelliteImageService.saveImage(image, endereco);

            if (imagePath == null) {
                response.put("ok", false);
                response.put("result", "error");
                response.put("mensagem_imagemdesatelite", "Erro ao salvar imagem");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }

            // 3. Enviar via WATI
            boolean enviado = satelliteImageService.sendViaWati(telefone, imagePath, endereco);

            if (!enviado) {
                // Deletar arquivo se falhar
                satelliteImageService.deleteFile(imagePath);
                satelliteImageService.deleteEmptyDirectory(endereco);

                response.put("ok", false);
                response.put("result", "error");
                response.put("mensagem_imagemdesatelite", "Erro ao enviar imagem via WATI");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }

            // 4. Deletar arquivo após envio bem-sucedido
            satelliteImageService.deleteFile(imagePath);
            satelliteImageService.deleteEmptyDirectory(endereco);

            logger.info("========================================================");
            logger.info("✓ PROCESSO CONCLUÍDO COM SUCESSO");
            logger.info("========================================================");

            response.put("ok", true);
            response.put("result", "success");
            response.put("mensagem_imagemdesatelite", "Imagem de satélite enviada com sucesso!");
            response.put("imagemdesatelite_url", imagePath);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("ERRO geral: " + e.getMessage());
            e.printStackTrace();

            response.put("ok", false);
            response.put("result", "error");
            response.put("mensagem_imagemdesatelite", "Erro interno do servidor: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Endpoint raiz - Retorna boas-vindas
     * GET /
     */
    @GetMapping
    @RequestMapping("/")
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
