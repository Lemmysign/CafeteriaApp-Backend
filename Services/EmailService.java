package Evercare_CafeteriaApp.Services;

public interface EmailService {


    void sendTransferNotification(String email, String subject, String message);
    void sendTransactionSuccessEmail(String recipientEmail, String reference, String amount);
}
