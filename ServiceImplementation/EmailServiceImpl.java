package Evercare_CafeteriaApp.ServiceImplementation;

import Evercare_CafeteriaApp.Model.Customer;
import Evercare_CafeteriaApp.Repository.CustomerRepoPackage.CustomerRepository;
import Evercare_CafeteriaApp.Services.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private CustomerRepository customerRepository;

    @Override
    @Async
    public void sendTransferNotification(String email, String subject, String message) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setTo(email);
            helper.setSubject(subject);
            helper.setText(message, true); // Second parameter 'true' indicates HTML content

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            // Handle exception
            throw new RuntimeException("Cannot send mail");
        }
    }

    @Override
    @Async
    public void sendTransactionSuccessEmail(String recipientEmail, String reference, String amount) {
        Customer customer = customerRepository.findByEmailIgnoreCase(recipientEmail)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(recipientEmail);
            helper.setSubject("Transaction Confirmation - Evercare Cafeteria");

            String emailContent =
                    "<!DOCTYPE html>" +
                            "<html lang='en'>" +
                            "<head>" +
                            "    <meta charset='UTF-8'>" +
                            "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                            "    <title>Transaction Confirmation</title>" +
                            "</head>" +
                            "<body style='margin: 0; padding: 0; font-family: \"Segoe UI\", Arial, sans-serif;'>" +
                            "    <div style='max-width: 600px; margin: 0 auto; padding: 0;'>" +
                            "        <!-- Header -->" +
                            "        <div style='background-color: #0078D4; padding: 20px; text-align: center;'>" +
                            "            <h1 style='color: white; margin: 0;'>Evercare Cafeteria</h1>" +
                            "        </div>" +
                            "        <!-- Main Content -->" +
                            "        <div style='background-color: #ffffff; padding: 30px; border-left: 1px solid #E0E0E0; border-right: 1px solid #E0E0E0;'>" +
                            "            <h2 style='color: #333333; margin-top: 0;'>Transaction Successful</h2>" +
                            "            <p style='color: #555555; line-height: 1.5;'>Dear " + customer.getName() + ",</p>" +
                            "            <p style='color: #555555; line-height: 1.5;'>Your wallet has been successfully credited with the following details:</p>" +
                            "            <div style='background-color: #F9F9F9; border-left: 4px solid #0078D4; padding: 15px; margin: 20px 0;'>" +
                            "                <p style='margin: 8px 0; color: #333333;'><strong>Amount:</strong> ₦" + amount + "</p>" +
                            "                <p style='margin: 8px 0; color: #333333;'><strong>Reference:</strong> " + reference + "</p>" +
                            "                <p style='margin: 8px 0; color: #333333;'><strong>Date:</strong> " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm:ss")) + "</p>" +
                            "            </div>" +
                            "            <p style='color: #555555; line-height: 1.5;'>Thank you for using Evercare Lekki Cafeteria services. If you have any questions about this transaction, please contact our customer service team.</p>" +
                            "        </div>" +
                            "        <!-- Footer -->" +
                            "        <div style='background-color: #F5F5F5; padding: 20px; text-align: center; border-bottom: 1px solid #E0E0E0; border-left: 1px solid #E0E0E0; border-right: 1px solid #E0E0E0;'>" +
                            "            <p style='margin: 5px 0; color: #666666; font-size: 14px;'>© " + java.time.Year.now().getValue() + " Evercare Hospital Lekki</p>" +
                            "            <p style='margin: 5px 0; color: #666666; font-size: 14px;'>This is an automated message. Please do not reply to this email.</p>" +
                            "        </div>" +
                            "    </div>" +
                            "</body>" +
                            "</html>";

            helper.setText(emailContent, true);
            helper.setFrom("innehlemuel18@gmail.com", "Evercare Cafeteria");

            mailSender.send(message);

        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new RuntimeException("Failed to send transaction confirmation email", e);
        }
    }



}