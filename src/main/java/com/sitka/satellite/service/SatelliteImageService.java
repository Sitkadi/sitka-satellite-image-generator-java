package com.sitka.satellite.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Service
public class SatelliteImageService {

    private static final Logger logger = LoggerFactory.getLogger(SatelliteImageService.class);

    @Value("${app.google.api-key}")
    private String googleApiKey;

    @Value("${app.wati.base-url}")
    private String watiBaseUrl;

    @Value("${app.wati.api-token}")
    private String watiApiToken;

    @Value("${app.wati.phone-id}")
    private String watiPhoneId;

    @Value("${app.temp-dir}")
    private String tempDir;

    public SatelliteImageService() {
        // Construtor vazio
    }

    /**
     * Gera imagem de satélite usando Google Maps API
     */
    public BufferedImage generateSatelliteImage(String endereco) {
        try {
            log("========================================================");
            log("GERANDO IMAGEM DE SATÉLITE");
            log("Endereço: " + endereco);
            log("========================================================");

            if (googleApiKey == null || googleApiKey.isEmpty()) {
                log("ERRO: GOOGLE_API_KEY não configurada!");
                return null;
            }

            // Codificar endereço para URL
            String encodedEndereco = URLEncoder.encode(endereco, StandardCharsets.UTF_8);
            
            String mapUrl = "https://maps.googleapis.com/maps/api/staticmap?" +
                    "center=" + encodedEndereco + "&" +
                    "zoom=18&" +
                    "size=600x600&" +
                    "maptype=satellite&" +
                    "markers=color:red%7C" + encodedEndereco + "&" +
                    "key=" + googleApiKey;

            log("Baixando imagem de: " + mapUrl);

            URL url = new URL(mapUrl);
            BufferedImage image = ImageIO.read(url);

            if (image == null) {
                log("ERRO: Imagem retornou null");
                return null;
            }

            log("✓ Imagem gerada com sucesso! Dimensões: " + image.getWidth() + "x" + image.getHeight());
            return image;

        } catch (Exception e) {
            log("ERRO ao gerar imagem: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Salva imagem em arquivo
     */
    public String saveImage(BufferedImage image, String endereco) {
        try {
            log("Salvando imagem...");

            // Criar diretório se não existir
            Files.createDirectories(Paths.get(tempDir));

            String dirPath = tempDir + endereco;
            String filePath = dirPath + "/satellite_image.png";

            Files.createDirectories(Paths.get(dirPath));

            File outputFile = new File(filePath);
            ImageIO.write(image, "png", outputFile);

            long fileSizeKB = Files.size(Paths.get(filePath)) / 1024;
            log("✓ Imagem salva em: " + filePath);
            log("Tamanho: " + fileSizeKB + " KB");

            return filePath;

        } catch (Exception e) {
            log("ERRO ao salvar imagem: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Envia imagem via WATI usando o endpoint correto
     * 
     * IMPORTANTE: O endpoint sendSessionFile espera:
     * - URL: /api/v1/sendSessionFile/{watiPhoneId}
     * - watiPhoneId: Número do WATI (quem envia)
     * - telefone: Número do destinatário (quem recebe)
     */
    public boolean sendViaWati(String telefone, String imagePath, String endereco) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            log("========================================================");
            log("ENVIANDO IMAGEM PARA WATI");
            log("========================================================");
            log("Telefone (destinatário): " + telefone);
            log("WATI Phone ID (remetente): " + watiPhoneId);

            // Validações
            if (watiApiToken == null || watiApiToken.isEmpty()) {
                log("ERRO: WATI_API_TOKEN não configurada!");
                return false;
            }

            if (watiPhoneId == null || watiPhoneId.isEmpty()) {
                log("ERRO: WATI_PHONE_ID não configurada!");
                return false;
            }

            if (telefone == null || telefone.isEmpty()) {
                log("ERRO: Telefone do destinatário não fornecido!");
                return false;
            }

            // Construir URL correta: /api/v1/sendSessionFile/{watiPhoneId}
            String url = watiBaseUrl + "/api/v1/sendSessionFile/" + watiPhoneId;
            log("URL do WATI: " + url);

            HttpPost httpPost = new HttpPost(url);

            // Adicionar header de autenticação
            httpPost.setHeader("Authorization", "Bearer " + watiApiToken);

            File imageFile = new File(imagePath);

            if (!imageFile.exists()) {
                log("ERRO: Arquivo de imagem não encontrado: " + imagePath);
                return false;
            }

            log("✓ Arquivo encontrado: " + imageFile.getAbsolutePath());

            // Construir multipart form data
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addBinaryBody("media", imageFile, org.apache.http.entity.ContentType.IMAGE_PNG, "satellite_image.png");
            builder.addTextBody("recipient", telefone);
            builder.addTextBody("caption", "Imagem de satélite do imóvel: " + endereco);

            HttpEntity entity = builder.build();
            httpPost.setEntity(entity);

            log("Enviando requisição POST...");

            CloseableHttpResponse response = httpClient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();

            String responseBody = EntityUtils.toString(response.getEntity());
            log("Status WATI: " + statusCode);
            log("Resposta WATI: " + responseBody);

            response.close();

            if (statusCode >= 200 && statusCode < 300) {
                log("✓ Imagem enviada com sucesso!");
                return true;
            } else {
                log("ERRO: WATI retornou status " + statusCode);
                
                // Tentar parsear resposta JSON para mais detalhes
                try {
                    JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
                    if (jsonResponse.has("info")) {
                        log("Detalhes do erro: " + jsonResponse.get("info").getAsString());
                    }
                } catch (Exception e) {
                    // Ignorar erro ao parsear JSON
                }
                
                return false;
            }

        } catch (Exception e) {
            log("ERRO ao enviar via WATI: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                log("ERRO ao fechar HttpClient: " + e.getMessage());
            }
        }
    }

    /**
     * Deleta arquivo de imagem
     */
    public void deleteFile(String filePath) {
        try {
            Files.deleteIfExists(Paths.get(filePath));
            log("✓ Arquivo deletado: " + filePath);
        } catch (Exception e) {
            log("ERRO ao deletar arquivo: " + e.getMessage());
        }
    }

    /**
     * Deleta diretório vazio
     */
    public void deleteEmptyDirectory(String dirPath) {
        try {
            File dir = new File(dirPath);
            if (dir.exists() && dir.isDirectory()) {
                File[] files = dir.listFiles();
                if (files != null && files.length == 0) {
                    Files.deleteIfExists(Paths.get(dirPath));
                    log("✓ Diretório vazio deletado: " + dirPath);
                }
            }
        } catch (Exception e) {
            log("ERRO ao deletar diretório: " + e.getMessage());
        }
    }

    /**
     * Log helper
     */
    private void log(String message) {
        logger.info(message);
    }

}
