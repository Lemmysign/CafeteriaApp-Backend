package Evercare_CafeteriaApp.DTO.PaymentDtoPackage;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class PaystackDTO {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class InitializeTransactionRequest {
        private BigDecimal amount;
        private String email;
        private String reference;
        private String callbackUrl;
        private String currency;
        private String channels;

        public InitializeTransactionRequest() {}

        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getReference() { return reference; }
        public void setReference(String reference) { this.reference = reference; }

        public String getCallbackUrl() { return callbackUrl; }
        public void setCallbackUrl(String callbackUrl) { this.callbackUrl = callbackUrl; }

        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }

        public String getChannels() { return channels; }
        public void setChannels(String channels) { this.channels = channels; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class InitializeTransactionResponse {
        private boolean status;
        private String message;
        private Data data;

        public boolean isStatus() { return status; }
        public void setStatus(boolean status) { this.status = status; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public Data getData() { return data; }
        public void setData(Data data) { this.data = data; }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Data {
            @JsonProperty("authorization_url")
            private String authorizationUrl;
            @JsonProperty("access_code")
            private String accessCode;
            private String reference;

            public String getAuthorizationUrl() { return authorizationUrl; }
            public void setAuthorizationUrl(String authorizationUrl) { this.authorizationUrl = authorizationUrl; }

            public String getAccessCode() { return accessCode; }
            public void setAccessCode(String accessCode) { this.accessCode = accessCode; }

            public String getReference() { return reference; }
            public void setReference(String reference) { this.reference = reference; }
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VerifyTransactionResponse {
        private boolean status;
        private String message;
        private Data data;

        public boolean isStatus() { return status; }
        public void setStatus(boolean status) { this.status = status; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public Data getData() { return data; }
        public void setData(Data data) { this.data = data; }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Data {
            private String status;
            private String reference;
            private BigDecimal amount;
            @JsonProperty("paid_at")
            private String paidAt;
            @JsonProperty("payment_method")
            private String paymentMethod;
            private Customer customer;
            private String currency;

            public String getStatus() { return status; }
            public void setStatus(String status) { this.status = status; }

            public String getReference() { return reference; }
            public void setReference(String reference) { this.reference = reference; }

            public BigDecimal getAmount() { return amount; }
            public void setAmount(BigDecimal amount) { this.amount = amount; }

            public String getPaidAt() { return paidAt; }
            public void setPaidAt(String paidAt) { this.paidAt = paidAt; }

            public String getPaymentMethod() { return paymentMethod; }
            public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

            public Customer getCustomer() { return customer; }
            public void setCustomer(Customer customer) { this.customer = customer; }

            public String getCurrency() { return currency; }
            public void setCurrency(String currency) { this.currency = currency; }
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Customer {
            private String email;

            public String getEmail() { return email; }
            public void setEmail(String email) { this.email = email; }
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WebhookEventData {
        private String event;
        private Data data;

        public String getEvent() { return event; }
        public void setEvent(String event) { this.event = event; }

        public Data getData() { return data; }
        public void setData(Data data) { this.data = data; }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Data {
            private String reference;
            private BigDecimal amount;
            private String status;

            public String getReference() { return reference; }
            public void setReference(String reference) { this.reference = reference; }

            public BigDecimal getAmount() { return amount; }
            public void setAmount(BigDecimal amount) { this.amount = amount; }

            public String getStatus() { return status; }
            public void setStatus(String status) { this.status = status; }
        }
    }

    public static class PaymentRequest {
        private BigDecimal amount;
        private String email;
        private String paymentChannel;

        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPaymentChannel() { return paymentChannel; }
        public void setPaymentChannel(String paymentChannel) { this.paymentChannel = paymentChannel; }
    }

}