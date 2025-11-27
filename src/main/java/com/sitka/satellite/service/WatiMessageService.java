package com.sitka.satellite.service;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.net.URLEncoder;

@Service
public class WatiMessageService {

    private static final Logger logger = LoggerFactory.getLogger(WatiMessageService.class);

    @Value("${app.wati.base-url:https://live.wati.io/1047617}")
    private String watiBaseUrl;

    @Value("${app.wati.api-token:}")
    private String watiApiToken;

    // Debug: Log das variáveis de ambiente
    public WatiMessageService() {
        logger.info("WATI_API_TOKEN env var: {}", System.getenv("WATI_API_TOKEN"));
        logger.info("APP_WATI_API_TOKEN env var: {}", System.getenv("APP_WATI_API_TOKEN"));
    }

    @Value("${app.wati.phone-id:}")
    private String watiPhoneId;

    /**
     * Enviar mensagem de texto simples via WATI
     */
    public Map<String, Object> sendTextMessage(String phoneNumber, String message) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Remover caracteres especiais do número
            String cleanPhoneNumber = phoneNumber.replaceAll("[^0-9]", "");

            // Criar cliente HTTP
            CloseableHttpClient httpClient = HttpClients.createDefault();

            // URL do endpoint WATI
            String encodedToken = URLEncoder.encode(watiApiToken, "UTF-8");
            String url = String.format("%s/sendSessionMessage?token=%s",
                    watiBaseUrl, encodedToken);

            // Criar request POST
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Content-Type", "application/json");

            // Criar payload JSON
            JSONObject payload = new JSONObject();
            payload.put("phoneNumber", cleanPhoneNumber);
            payload.put("message", message);

            // Adicionar payload ao request
            httpPost.setEntity(new StringEntity(payload.toString(), "UTF-8"));

            // Executar request
            CloseableHttpResponse httpResponse = httpClient.execute(httpPost);

            // Obter response
            String responseBody = EntityUtils.toString(httpResponse.getEntity());
            int statusCode = httpResponse.getStatusLine().getStatusCode();

            // Log de debug
            logger.info("WATI Request URL: {}", url);
            logger.info("WATI Request Payload: {}", payload.toString());
            logger.info("WATI Response - Status: {}, Body: {}", statusCode, responseBody);

            // Processar response
            if (statusCode >= 200 && statusCode < 300) {
                response.put("ok", true);
                response.put("message", "Mensagem enviada com sucesso!");
                response.put("status_code", statusCode);
                response.put("wati_response", new JSONObject(responseBody).toMap());
            } else {
                response.put("ok", false);
                response.put("message", "Erro ao enviar mensagem");
                response.put("status_code", statusCode);
                response.put("error", responseBody);
            }

            // Fechar recursos
            httpResponse.close();
            httpClient.close();

        } catch (IOException e) {
            logger.error("IOException ao conectar com WATI", e);
            response.put("ok", false);
            response.put("message", "Erro ao conectar com WATI");
            response.put("error", e.getMessage());
        } catch (Exception e) {
            logger.error("Exception ao processar requisição", e);
            response.put("ok", false);
            response.put("message", "Erro ao processar requisição");
            response.put("error", e.getMessage());
        }

        return response;
    }

    /**
     * Enviar mensagem com template
     */
    public Map<String, Object> sendTemplateMessage(String phoneNumber, String templateName, Map<String, String> parameters) {
        Map<String, Object> response = new HashMap<>();

        try {
            String cleanPhoneNumber = phoneNumber.replaceAll("[^0-9]", "");

            CloseableHttpClient httpClient = HttpClients.createDefault();

            String encodedToken = URLEncoder.encode(watiApiToken, "UTF-8");
            String url = String.format("%s/sendSessionMessage?token=%s",
                    watiBaseUrl, encodedToken);

            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Content-Type", "application/json");

            JSONObject payload = new JSONObject();
            payload.put("phoneNumber", cleanPhoneNumber);
            payload.put("template", templateName);
            if (parameters != null) {
                payload.put("parameters", parameters);
            }

            httpPost.setEntity(new StringEntity(payload.toString(), "UTF-8"));

            CloseableHttpResponse httpResponse = httpClient.execute(httpPost);

            String responseBody = EntityUtils.toString(httpResponse.getEntity());
            int statusCode = httpResponse.getStatusLine().getStatusCode();

            // Log de debug
            logger.info("WATI Template Request URL: {}", url);
            logger.info("WATI Template Request Payload: {}", payload.toString());
            logger.info("WATI Template Response - Status: {}, Body: {}", statusCode, responseBody);

            if (statusCode >= 200 && statusCode < 300) {
                response.put("ok", true);
                response.put("message", "Mensagem de template enviada com sucesso!");
                response.put("status_code", statusCode);
                response.put("wati_response", new JSONObject(responseBody).toMap());
            } else {
                response.put("ok", false);
                response.put("message", "Erro ao enviar mensagem de template");
                response.put("status_code", statusCode);
                response.put("error", responseBody);
            }

            httpResponse.close();
            httpClient.close();

        } catch (Exception e) {
            response.put("ok", false);
            response.put("message", "Erro ao processar requisição");
            response.put("error", e.getMessage());
        }

        return response;
    }
}
