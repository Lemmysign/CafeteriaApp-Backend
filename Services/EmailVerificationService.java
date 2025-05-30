package Evercare_CafeteriaApp.Services;

public interface EmailVerificationService {

    void sendVerificationEmail(String recipientEmail, String verificationUrl);


}
