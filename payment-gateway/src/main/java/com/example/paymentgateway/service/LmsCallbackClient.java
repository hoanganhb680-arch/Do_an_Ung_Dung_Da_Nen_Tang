package com.example.paymentgateway.service;

import com.example.paymentgateway.dto.request.IpnCallbackRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@Slf4j
public class LmsCallbackClient {

    public void notifyLms(String ipnUrl, IpnCallbackRequest request, String apiKey, String signature) {
        try {
            RestClient.create()
                    .post()
                    .uri(ipnUrl)
                    .header("X-Api-Key", apiKey)
                    .header("X-Signature", signature)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception ex) {
            log.warn("Cannot send callback to LMS: {}", ex.getMessage());
        }
    }
}
