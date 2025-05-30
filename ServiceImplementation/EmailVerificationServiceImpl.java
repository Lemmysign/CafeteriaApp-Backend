package Evercare_CafeteriaApp.ServiceImplementation;
import Evercare_CafeteriaApp.Model.Customer;
import Evercare_CafeteriaApp.Repository.CustomerRepoPackage.CustomerRepository;
import Evercare_CafeteriaApp.Services.EmailVerificationService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailVerificationServiceImpl implements EmailVerificationService {

    private final JavaMailSender mailSender;
    private final CustomerRepository customerRepository;

    public EmailVerificationServiceImpl(JavaMailSender mailSender, CustomerRepository customerRepository) {
        this.mailSender = mailSender;
        this.customerRepository = customerRepository;
    }


    @Override
    @Async
    public void sendVerificationEmail(String recipientEmail, String verificationUrl) {
        Customer customer = customerRepository.findByEmailIgnoreCase(recipientEmail).orElseThrow(()->new RuntimeException("Email not found"));
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(recipientEmail);
            helper.setSubject("Evercare Cafeteria Email Verification");

            // HTML content with improved design using brand colors
            String emailContent =
                    "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 0; border-radius: 8px; box-shadow: 0 4px 8px rgba(0,0,0,0.1);'>" +
                            "<div style='background-color: #062A55; padding: 25px; text-align: center; border-radius: 8px 8px 0 0;'>" +
                            "<h1 style='color: white; margin: 0; font-weight: 600; font-size: 22px;'>EVERCARE CAFETERIA</h1>" +
                            "</div>" +
                            "<div style='background-color: white; padding: 30px; border-radius: 0 0 8px 8px;'>" +
                            "<p style='font-size: 16px; color: #333; margin-top: 0;'>Hello <span style='font-weight: bold;'>" + customer.getName() + "</span>,</p>" +
                            "<p style='font-size: 16px; color: #333; line-height: 1.5;'>Welcome to Evercare Lekki Cafeteria! Please verify your email address to activate your account.</p>" +
                            "<div style='text-align: center; margin: 30px 0;'>" +
                            "<a href='" + verificationUrl + "' style='background-color: #8043C8; color: white; padding: 12px 30px; " +
                            "text-decoration: none; border-radius: 5px; font-weight: bold; display: inline-block; font-size: 16px;'>Confirm Email</a>" +
                            "</div>" +
                            "<p style='font-size: 14px; color: #555; line-height: 1.5;'>If you did not request this, please ignore this email.</p>" +
                            "<p style='font-size: 14px; color: #555; line-height: 1.5;'>This link will expire in <strong>3 hours</strong>.</p>" +
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
            throw new RuntimeException("Failed to send verification email", e);
        }
    }
}
