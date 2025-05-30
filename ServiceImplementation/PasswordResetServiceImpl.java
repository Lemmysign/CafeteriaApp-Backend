package Evercare_CafeteriaApp.ServiceImplementation;

import Evercare_CafeteriaApp.Model.Customer;
import Evercare_CafeteriaApp.Model.PasswordResetToken;
import Evercare_CafeteriaApp.Model.Server;
import Evercare_CafeteriaApp.Repository.CustomerRepoPackage.CustomerRepository;
import Evercare_CafeteriaApp.Repository.CustomerRepoPackage.PasswordResetTokenRepository;
import Evercare_CafeteriaApp.Repository.ServiceRepoPackage.ServerRepository;
import Evercare_CafeteriaApp.Services.PasswordResetService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetServiceImpl implements PasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;
    private final CustomerRepository customerRepository;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;
    private final ServerRepository serverRepository;

    @Autowired
    public PasswordResetServiceImpl(PasswordResetTokenRepository tokenRepository, CustomerRepository customerRepository, JavaMailSender mailSender, PasswordEncoder passwordEncoder, ServerRepository serverRepository) {
        this.tokenRepository = tokenRepository;
        this.customerRepository = customerRepository;
        this.mailSender = mailSender;
        this.passwordEncoder = passwordEncoder;
        this.serverRepository = serverRepository;
    }

    @Override
    public void sendResetPasswordEmail(String email) {
        // Try to find either a customer or server with this email
        Optional<Customer> customerOpt = customerRepository.findByEmailIgnoreCase(email);
        Optional<Server> serverOpt = serverRepository.findByEmailIgnoreCase(email);

        String name;
        boolean isServer = false;

        // Determine if it's a customer or server email
        if (customerOpt.isPresent()) {
            name = customerOpt.get().getName();
        } else if (serverOpt.isPresent()) {
            name = serverOpt.get().getServerName();
            isServer = true;
        } else {
            throw new RuntimeException("Email not found!");
        }

        // Delete old token
        tokenRepository.deleteByEmail(email);

        // Generate new token
        String token = UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(10);
        tokenRepository.save(new PasswordResetToken(token, email, expiryDate));

        // Send email with appropriate user type
        sendEmail(email, token, name, isServer);
    }



    @Override
    public void sendEmail(String email, String token, String name, boolean isServer) {
        // Update the reset link to point to the frontend
        String resetLink = "https://cafeteriadash.netlify.app/resetpassword?token=" + token;
        String userType = isServer ? "Server" : "Customer";

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(email);
            helper.setSubject("Evercare Cafeteria Account Password Reset");

            // HTML content with improved design using brand colors
            String emailContent =
                    "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 0; border-radius: 8px; box-shadow: 0 4px 8px rgba(0,0,0,0.1);'>" +
                            "<div style='background-color: #062A55; padding: 25px; text-align: center; border-radius: 8px 8px 0 0;'>" +
                            "<h1 style='color: white; margin: 0; font-weight: 600; font-size: 22px;'>EVERCARE CAFETERIA</h1>" +
                            "</div>" +
                            "<div style='background-color: white; padding: 30px; border-radius: 0 0 8px 8px;'>" +
                            "<p style='font-size: 16px; color: #333; margin-top: 0;'>Hello <span style='font-weight: bold;'>" + name + "</span>,</p>" +
                            "<p style='font-size: 16px; color: #333; line-height: 1.5;'>You requested a password reset for your " + userType + " Cafeteria account. Click the button below to set a new password.</p>" +
                            "<div style='text-align: center; margin: 30px 0;'>" +
                            "<a href='" + resetLink + "' style='background-color: #8043C8; color: white; padding: 12px 30px; " +
                            "text-decoration: none; border-radius: 5px; font-weight: bold; display: inline-block; font-size: 16px;'>Reset Password</a>" +
                            "</div>" +
                            "<p style='font-size: 14px; color: #555; line-height: 1.5;'>If you did not request this, please ignore this email.</p>" +
                            "<p style='font-size: 14px; color: #555; line-height: 1.5;'>This link will expire in <strong>10 minutes</strong>.</p>" +
                            "<div style='margin-top: 30px; padding-top: 20px; border-top: 1px solid #eee;'>" +
                            "<p style='text-align: center; margin: 0;'>" +
                            "<span style='display: block; color: #98CB4F; font-weight: bold; margin-bottom: 5px; font-size: 16px;'>Evercare Lekki Cafeteria</span>" +
                            "<span style='display: block; color: #666; font-size: 12px;'>ICT Team</span>" +
                            "</p>" +
                            "</div>" +
                            "</div>" +
                            "</div>";

            helper.setText(emailContent, true); // Enable HTML content

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }

    @Override
    public void validateToken(String token) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token!"));

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(resetToken); // Clean up expired token
            throw new RuntimeException("Token has expired!");
        }
    }


    @Override
    public String resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token!"));

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(resetToken); // Clean up expired token
            throw new RuntimeException("Token has expired!");
        }

        String email = resetToken.getEmail();
        String userType;

        // Try to find customer by email
        Optional<Customer> customerOpt = customerRepository.findByEmailIgnoreCase(email);
        if (customerOpt.isPresent()) {
            // Update customer password
            Customer customer = customerOpt.get();
            customer.setPassword(passwordEncoder.encode(newPassword));
            customerRepository.save(customer);
            userType = "Customer";
        } else {
            // Try to find server by email
            Optional<Server> serverOpt = serverRepository.findByEmailIgnoreCase(email);
            if (serverOpt.isPresent()) {
                // Update server password
                Server server = serverOpt.get();
                server.setServerPassword(passwordEncoder.encode(newPassword));
                serverRepository.save(server);
                userType = "Server";
            } else {
                // If neither customer nor server is found
                throw new RuntimeException("User not found for email: " + email);
            }
        }

        // Delete token after use
        tokenRepository.delete(resetToken);

        return userType + " password successfully reset!";
    }

    public ServerRepository getServerRepository() {
        return serverRepository;
    }

}


