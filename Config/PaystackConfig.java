package Evercare_CafeteriaApp.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class PaystackConfig {

    @Value("${paystack.secretKey}")
    private String paystackSecretKey;

    @Value("${paystack.publicKey}")
    private String paystackPublicKey;

    @Value("${paystack.baseUrl:https://api.paystack.co}")
    private String paystackBaseUrl;

    public String getPaystackSecretKey() {
        return paystackSecretKey;
    }

    public void setPaystackSecretKey(String paystackSecretKey) {
        this.paystackSecretKey = paystackSecretKey;
    }

    public String getPaystackBaseUrl() {
        return paystackBaseUrl;
    }

    @Bean
    public RestTemplate paystackRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add((request, body, execution) -> {
            request.getHeaders().set("Authorization", "Bearer " + paystackSecretKey);
            request.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            return execution.execute(request, body);
        });
        return restTemplate;
    }

    // Keep WebClient for backward compatibility or remove if not needed
    @Bean
    public WebClient paystackWebClient() {
        return WebClient.builder()
                .baseUrl(paystackBaseUrl)
                .defaultHeader("Authorization", "Bearer " + paystackSecretKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    public String getPaystackPublicKey() {
        return paystackPublicKey;
    }
}