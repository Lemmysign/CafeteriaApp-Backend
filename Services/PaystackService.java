package Evercare_CafeteriaApp.Services;

import Evercare_CafeteriaApp.DTO.PaymentDtoPackage.PaystackDTO;

public interface PaystackService {

    PaystackDTO.InitializeTransactionResponse initializeTransaction(PaystackDTO.PaymentRequest paymentRequest, Long customerId);
    boolean verifyTransaction(String reference);
    boolean processWebhookEvent(PaystackDTO.WebhookEventData webhookEvent);
    boolean updateCustomerBalanceAndTransaction(String reference, PaystackDTO.VerifyTransactionResponse response);
}
