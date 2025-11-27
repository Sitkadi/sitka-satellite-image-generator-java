package com.sitka.satellite.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;

@Service
public class GoogleMapsService {

    private static final Logger logger = LoggerFactory.getLogger(GoogleMapsService.class);

    @Value("${app.google.api-key:}")
    private String googleApiKey;

    public File getSatelliteImage(String address) {
        try {
            String encodedAddress = URLEncoder.encode(address, "UTF-8");
            String imageUrl = String.format(
                "https://maps.googleapis.com/maps/api/staticmap?center=%s&zoom=15&size=600x400&maptype=satellite&key=%s",
                encodedAddress, googleApiKey);

            URL url = new URL(imageUrl);
            InputStream in = url.openStream();

            File tempFile = File.createTempFile("satellite-", ".png");
            FileOutputStream out = new FileOutputStream(tempFile);

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }

            in.close();
            out.close();

            logger.info("Imagem de satélite gerada com sucesso para o endereço: {}", address);
            return tempFile;

        } catch (Exception e) {
            logger.error("Erro ao gerar imagem de satélite", e);
            return null;
        }
    }
}
