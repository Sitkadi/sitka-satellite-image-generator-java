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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class WatiMessageService {

    @Value("${wati.api.url:https://api.wati.io/api/v1}")
    private String watiApiUrl;

    @Value("${wati.api.token:}")
    private String watiApiToken;

    @Value("${wati.phone.id:}")
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
            String url = String.format("%s/sendSessionMessage?token=%s",
                    watiApiUrl, watiApiToken);

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
            response.put("ok", false);
            response.put("message", "Erro ao conectar com WATI");
            response.put("error", e.getMessage());
        } catch (Exception e) {
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

            String url = String.format("%s/sendSessionMessage?token=%s",
                    watiApiUrl, watiApiToken);

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
