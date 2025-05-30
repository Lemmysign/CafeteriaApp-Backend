package Evercare_CafeteriaApp.Services;

public interface PasswordResetService {



    void sendResetPasswordEmail(String email);
    void sendEmail(String email, String token, String name, boolean isServer);
    String resetPassword(String token, String newPassword);
    void validateToken(String token);

}
