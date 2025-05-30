package Evercare_CafeteriaApp.Controller;

import Evercare_CafeteriaApp.DTO.CustomerDtoPackage.CustomerRegisterDTO;
import Evercare_CafeteriaApp.Model.Customer;
import Evercare_CafeteriaApp.Model.EmailVerificationToken;
import Evercare_CafeteriaApp.Repository.CustomerRepoPackage.CustomerRepository;
import Evercare_CafeteriaApp.Repository.CustomerRepoPackage.EmailVerificationTokenRepository;
import Evercare_CafeteriaApp.Services.CustomerService;
import Evercare_CafeteriaApp.Services.PasswordResetService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/customers")
public class CustomerRestController {

    // DEPENDENCIES
    private final CustomerService customerService;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final CustomerRepository customerRepository;
    private final PasswordResetService passwordResetService;

    public CustomerRestController(
            CustomerService customerService,
            EmailVerificationTokenRepository emailVerificationTokenRepository,
            CustomerRepository customerRepository,
            PasswordResetService passwordResetService) {
        this.customerService = customerService;
        this.emailVerificationTokenRepository = emailVerificationTokenRepository;
        this.customerRepository = customerRepository;
        this.passwordResetService = passwordResetService;
    }

    // Customer Registration
    @PostMapping("/register")
    public ResponseEntity<String> registerCustomer(@RequestBody CustomerRegisterDTO customerRegisterDTO, HttpServletRequest request) {
        Customer registeredCustomer = customerService.register(customerRegisterDTO);

        // Invalidate old session (if any)
        HttpSession existingSession = request.getSession(false);
        if (existingSession != null) {
            existingSession.invalidate();
        }

        // Create a new session for the registered user
        HttpSession session = request.getSession(true);
        session.setAttribute("customerEmail", registeredCustomer.getEmail());

        return ResponseEntity.ok("Customer registered successfully: " + registeredCustomer.getEmail());
    }

    // Email Verification
    @GetMapping("/verify")
    public ResponseEntity<Map<String, String>> verifyEmail(@RequestParam String token) {
        Optional<EmailVerificationToken> optionalToken = emailVerificationTokenRepository.findByToken(token);

        if (optionalToken.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid or expired token."));
        }

        EmailVerificationToken emailVerificationToken = optionalToken.get();
        Customer customer = emailVerificationToken.getCustomer();

        customer.setVerified(true);
        customer.setEmailVerificationToken(null);
        customerRepository.save(customer);

        emailVerificationTokenRepository.delete(emailVerificationToken);
        emailVerificationTokenRepository.flush();

        boolean tokenExists = emailVerificationTokenRepository.existsByToken(token);
        if (tokenExists) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Error: Token deletion failed!"));
        }
        return ResponseEntity.ok().body(Map.of("message", "Email verified successfully!"));
    }

    // Customer Login
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @RequestParam String email,
            @RequestParam String password,
            HttpServletRequest request) {
        try {
            // Invalidate previous session
            HttpSession oldSession = request.getSession(false);
            if (oldSession != null) {
                oldSession.invalidate();
            }

            // Authenticate user
            Customer customer = customerService.login(email, password);

            customer.setLastActionTime(LocalDateTime.now());
            customerRepository.save(customer);

            // Create new session and store user info
            HttpSession newSession = request.getSession(true);
            newSession.setAttribute("customerId", customer.getId());
            newSession.setAttribute("customerEmail", customer.getEmail());

            // Prepare response
            Map<String, Object> response = new HashMap<>();
            response.put("name", customer.getName());
            response.put("id", customer.getId());
            response.put("balance", customer.getBalance() != null ? customer.getBalance() : BigDecimal.ZERO);
            response.put("locked", customer.isLocked()); // Added locked status
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Customer Logout
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate(); // Destroy session
        }

        response.setHeader("Set-Cookie", "JSESSIONID=; Path=/; HttpOnly; Max-Age=0");
        return ResponseEntity.ok("Logged out successfully.");
    }

    // Get Current Customer Details
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentCustomer(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("customerEmail") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not logged in");
        }

        String email = (String) session.getAttribute("customerEmail");

        try {
            Customer customer = customerService.findByEmail(email);
            Map<String, Object> response = new HashMap<>();
            response.put("id", customer.getId());
            response.put("name", customer.getName());
            response.put("email", customer.getEmail());
            response.put("balance", customer.getBalance() != null ? customer.getBalance() : BigDecimal.ZERO);
            response.put("locked", customer.isLocked()); // Added locked status

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Toggle Customer Lock
    @PutMapping("/{id}/toggle-lock")
    public ResponseEntity<Map<String, Object>> toggleCustomerLock(@PathVariable Long id) {
        boolean isLocked = customerService.toggleAccountLock(id);

        Map<String, Object> response = new HashMap<>();
        response.put("isLocked", isLocked);
        response.put("message", isLocked ? "account locked" : "account unlocked");

        return ResponseEntity.ok(response);
    }

    // Get Account Lock Status (Optional: Can be deprecated if not used elsewhere)
    @GetMapping("/{id}/lock-status")
    public ResponseEntity<Map<String, Boolean>> getAccountLockStatus(@PathVariable Long id) {
        Boolean isLocked = customerService.isAccountLocked(id);
        Map<String, Boolean> response = new HashMap<>();
        response.put("isLocked", isLocked);
        return ResponseEntity.ok(response);
    }

    // Transfer Credit
    @PostMapping("/transfer")
    public ResponseEntity<String> transferCredit(
            @RequestParam Long receiverId,
            @RequestParam BigDecimal amount,
            HttpServletRequest request) {
        String response = customerService.transferCredit(receiverId, amount, request);
        return ResponseEntity.ok(response);
    }

    // Validate Receiver
    @GetMapping("/validate-receiver/{receiverId}")
    public ResponseEntity<String> validateReceiver(@PathVariable Long receiverId) {
        String receiverName = customerService.getReceiverName(receiverId);
        return ResponseEntity.ok(receiverName);
    }

    // Forgot Password
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        try {
            passwordResetService.sendResetPasswordEmail(email);
            return ResponseEntity.ok().body(Map.of("message", "Password reset email sent successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Validate Reset Token
    @GetMapping("/reset-password")
    public ResponseEntity<?> validateResetToken(@RequestParam String token) {
        try {
            passwordResetService.validateToken(token);
            return ResponseEntity.ok().body(Map.of(
                    "redirectUrl", "https://cafeteriadash.netlify.app/reset-password?token=" + token,
                    "valid", true
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage(), "valid", false));
        }
    }

    // Reset Password
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("newPassword");

        try {
            String result = passwordResetService.resetPassword(token, newPassword);
            return ResponseEntity.ok().body(Map.of("message", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}