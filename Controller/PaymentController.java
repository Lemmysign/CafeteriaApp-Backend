package Evercare_CafeteriaApp.Controller;

import Evercare_CafeteriaApp.Config.PaystackConfig;
import Evercare_CafeteriaApp.DTO.PaymentDtoPackage.PaystackDTO;
import Evercare_CafeteriaApp.Model.Customer;
import Evercare_CafeteriaApp.Model.PaymentTransaction;
import Evercare_CafeteriaApp.Repository.CustomerRepoPackage.CustomerRepository;
import Evercare_CafeteriaApp.Repository.PaymentRepoPackage.PaymentTransactionRepository;
import Evercare_CafeteriaApp.Services.PaystackService;
import Evercare_CafeteriaApp.WebhookQueueProcessor.WebhookQueueProcessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaystackService paystackService;

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(PaymentController.class);

    @Autowired
    private PaystackConfig paystackConfig;

    @Autowired
    private WebhookQueueProcessor webhookQueueProcessor;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PaymentTransactionRepository paymentTransactionRepository;

    // Return public key for frontend
    @GetMapping("/config")
    public ResponseEntity<Map<String, String>> getPaymentConfig() {
        Map<String, String> config = new HashMap<>();
        config.put("publicKey", paystackConfig.getPaystackPublicKey());
        return ResponseEntity.ok(config);
    }

    // Start payment transaction
    @PostMapping("/initialize")
    public ResponseEntity<?> initializePayment(@RequestBody PaystackDTO.PaymentRequest paymentRequest,
                                               HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }

        Long customerId = (Long) session.getAttribute("customerId");
        if (customerId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }

        try {
            PaystackDTO.InitializeTransactionResponse response =
                    paystackService.initializeTransaction(paymentRequest, customerId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error initializing payment: " + e.getMessage());
        }
    }

    // Manual verification if needed
    @GetMapping("/verify/{reference}")
    public ResponseEntity<?> verifyPayment(@PathVariable String reference) {
        boolean verified = paystackService.verifyTransaction(reference);
        if (verified) {
            return ResponseEntity.ok("Payment verified successfully");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Payment verification failed");
        }
    }

    // Paystack will call this endpoint when transaction completes (async)
    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String rawBody,
            @RequestHeader(value = "X-Paystack-Signature", required = false) String paystackSignature
    ) {
        if (paystackSignature == null || paystackSignature.isBlank()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Signature missing or invalid");
        }

        try {
            boolean isValid = verifySignature(rawBody, paystackSignature, paystackConfig.getPaystackSecretKey());
            if (!isValid) {
                logger.warn("Invalid webhook signature received");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Signature validation failed");
            }

            webhookQueueProcessor.enqueue(rawBody);
            return ResponseEntity.ok("Webhook accepted for processing");

        } catch (Exception e) {
            logger.error("Error enqueuing webhook", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred");
        }
    }

    private boolean verifySignature(String payload, String receivedSignature, String secretKey) {
        try {
            Mac sha512_HMAC = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            sha512_HMAC.init(secretKeySpec);
            byte[] calculatedHash = sha512_HMAC.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String calculatedSignature = bytesToHex(calculatedHash);

            // Use constant-time comparison to prevent timing attacks
            return MessageDigest.isEqual(
                    calculatedSignature.toLowerCase().getBytes(StandardCharsets.UTF_8),
                    receivedSignature.toLowerCase().getBytes(StandardCharsets.UTF_8)
            );
        } catch (Exception e) {
            logger.error("Signature verification error", e);
            return false;
        }
    }


    // Authenticated customers can view their payment history
    @GetMapping("/history")
    public ResponseEntity<?> getPaymentHistory(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }

        Long customerId = (Long) session.getAttribute("customerId");
        if (customerId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }

        Optional<Customer> optionalCustomer = customerRepository.findById(customerId);
        if (optionalCustomer.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Customer not found");
        }

        List<PaymentTransaction> transactions =
                paymentTransactionRepository.findByCustomerOrderByCreatedAtDesc(optionalCustomer.get());

        return ResponseEntity.ok(transactions);
    }

    // Paystack redirects the user here after payment
    @GetMapping("/callback")
    public ResponseEntity<?> handleCallback(@RequestParam String reference) {
        boolean verified = paystackService.verifyTransaction(reference);

        if (verified) {

            return ResponseEntity.ok("Payment successful and verified.");
        } else {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Payment failed or could not be verified.");
        }
    }


    private String computeHmacSha512(String data, String secretKey) {
        try {
            Mac sha512_HMAC = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            sha512_HMAC.init(secretKeySpec);
            byte[] hash = sha512_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Unable to compute HMAC SHA512 hash", e);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder(2 * bytes.length);
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
