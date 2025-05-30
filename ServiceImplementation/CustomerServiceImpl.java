package Evercare_CafeteriaApp.ServiceImplementation;
import Evercare_CafeteriaApp.DTO.CustomerDtoPackage.CustomerRegisterDTO;
import Evercare_CafeteriaApp.DTO.CustomerDtoPackage.DisplayUserNameIdDTO;
import Evercare_CafeteriaApp.Mapper.CustomerMapper;
import Evercare_CafeteriaApp.Model.Customer;
import Evercare_CafeteriaApp.Model.EmailVerificationToken;
import Evercare_CafeteriaApp.Model.Role;
import Evercare_CafeteriaApp.Repository.CustomerRepoPackage.CustomerRepository;
import Evercare_CafeteriaApp.Repository.CustomerRepoPackage.EmailVerificationTokenRepository;
import Evercare_CafeteriaApp.Repository.RoleRepository;
import Evercare_CafeteriaApp.Services.CustomerService;
import Evercare_CafeteriaApp.Services.EmailService;
import Evercare_CafeteriaApp.Services.EmailVerificationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class CustomerServiceImpl implements CustomerService {

    //DEPENDENCIES
    private final PasswordEncoder passwordEncoder;
    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final RoleRepository roleRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final EmailService emailService;
    private final EmailVerificationService emailVerificationService;

    @Autowired
    public CustomerServiceImpl(PasswordEncoder passwordEncoder, CustomerRepository customerRepository, CustomerMapper customerMapper, JavaMailSender mailSender, RoleRepository roleRepository, EmailVerificationTokenRepository emailVerificationTokenRepository, EmailService emailService, EmailVerificationService emailVerificationService) {
        this.passwordEncoder = passwordEncoder;
        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
        this.roleRepository = roleRepository;
        this.emailVerificationTokenRepository = emailVerificationTokenRepository;
        this.emailService = emailService;
        this.emailVerificationService = emailVerificationService;
    }


    //SERVICE METHOD TO LOGIN
    @Override
    public Customer login(String email, String password) {
        Customer customer = customerRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new SecurityException("Invalid email or password!"));

        // First-time login: If the password is stored in plaintext, hash it before checking
        if (!customer.getPassword().startsWith("$2a$")) {
            if (customer.getPassword().equals(password)) {
                customer.setPassword(passwordEncoder.encode(password));
                customerRepository.save(customer);
            } else {
                throw new SecurityException("Invalid email or password!");
            }
        }

        // Validate the hashed password
        if (!passwordEncoder.matches(password, customer.getPassword())) {
            throw new SecurityException("Invalid email or password!");
        }

        // Check if the account is blocked
        if (Boolean.TRUE.equals(customer.isBlocked())) {
            throw new IllegalStateException("Account is Blocked. Please contact support.");
        }

        if (customer.isVerified()) {
            throw new RuntimeException("Please verify your email before logging in.");
        }
        customerMapper.toDTO(customer);
        return customer;
    }


//SERVICE METHOD TO REGISTER
@Override
public Customer register(CustomerRegisterDTO customerRegisterDTO) {
    if (customerRepository.existsByEmail(customerRegisterDTO.getEmail())) {
        throw new RuntimeException("Email already exists! Please use a different email.");
    }
    if (customerRepository.existsById(customerRegisterDTO.getId())) {
        throw new IllegalArgumentException("ID already exists. Please choose a different one.");
    }

    Role customerRole = roleRepository.findByRoleName("CUSTOMER")
            .orElseThrow(() -> new RuntimeException("Default role CUSTOMER not found!"));

    Customer customer = customerMapper.toEntity(customerRegisterDTO);
    customer.setPassword(passwordEncoder.encode(customerRegisterDTO.getPassword())); // Encode password before saving
    customer.setBalance(BigDecimal.ZERO);

    customer.setRole(customerRole);
    customer.setVerified(false);

    // **EXPLICITLY SAVE CUSTOMER FIRST**
    customer = customerRepository.save(customer);

    // **NOW generate and save verification token**
    String token = UUID.randomUUID().toString();
    EmailVerificationToken emailVerificationToken = new EmailVerificationToken(token, customer);
    emailVerificationToken.setExpiryDate(LocalDateTime.now().plusHours(3));
    emailVerificationTokenRepository.save(emailVerificationToken); //

    // Send verification email
    String verificationUrl = "https://cafeteriadash.netlify.app/verify?token=" + token;
    emailVerificationService.sendVerificationEmail(customer.getEmail(), verificationUrl);

    return customer;
}



    //SERVICE METHOD TO LOGOUT
    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response) {

        HttpSession session = request.getSession(false); // Get current session if exists
        if (session != null) {
            session.invalidate(); // Invalidate the session
        }
    }



    //SERVICE METHOD TO LOCK/UNLOCK ACCOUNT
    @Override
    public Boolean toggleAccountLock(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invalid id!"));

        boolean currentlyLocked = customer.isLocked();
        boolean newLockStatus = !currentlyLocked;

        customer.setLocked(newLockStatus);

        if (!newLockStatus) {
            // If the account is being unlocked now, record the time
            customer.setLastActionTime(LocalDateTime.now());
        }

        customerRepository.save(customer);

        return customer.isLocked();
    }

    @Override
    public Boolean isAccountLocked(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + id));

        return customer.isLocked();
    }


    @Override
    public String transferCredit(Long receiverId, BigDecimal amount, HttpServletRequest request) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {  // Correct way to compare BigDecimal
            throw new RuntimeException("Invalid transfer amount! Amount must be greater than zero.");
        }

        // Get the logged-in sender from the session
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("customerEmail") == null) {
            throw new RuntimeException("Unauthorized! Please log in.");
        }

        String senderEmail = (String) session.getAttribute("customerEmail");
        Customer sender = customerRepository.findByEmailIgnoreCase(senderEmail)
                .orElseThrow(() -> new RuntimeException("Sender not found!"));

        // Fetch receiver
        Customer receiver = customerRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Receiver not found!"));

        // Ensure sender has enough balance
        if (sender.getBalance().compareTo(amount) < 0) {  // Correct way to check balance
            throw new RuntimeException("Insufficient balance!");
        }

        if (sender.getId().equals(receiver.getId())) {
            throw new RuntimeException("You cannot transfer funds to yourself!");
        }
        if (receiver.isVerified()) {
            throw new RuntimeException("You cannot send to unverified recipients.");
        }

        // Perform transfer
        sender.setBalance(sender.getBalance().subtract(amount)); // Use BigDecimal.subtract()
        receiver.setBalance(receiver.getBalance().add(amount)); // Use BigDecimal.add()

        // Save updated balances
        customerRepository.save(sender);
        customerRepository.save(receiver);

        sendTransferEmails(sender, receiver, amount);

        return "Transaction successful! ₦" + amount + " has been transferred to " + receiver.getName();

    }

    @Override
    public void sendTransferEmails(Customer sender, Customer receiver, BigDecimal amount) {
        // Format date and time
        String formattedDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm:ss"));

        // Email to sender
        String senderSubject = "Cafeteria Transfer Confirmation: ₦" + amount + " Sent Successfully";
        String senderMessage = String.format(
                "<!DOCTYPE html>\n" +
                        "<html>\n" +
                        "<head>\n" +
                        "    <meta charset=\"UTF-8\">\n" +
                        "    <title>Transfer Confirmation</title>\n" +
                        "    <style>\n" +
                        "        body {\n" +
                        "            font-family: Arial, sans-serif;\n" +
                        "            line-height: 1.6;\n" +
                        "            color: #333333;\n" +
                        "            max-width: 600px;\n" +
                        "            margin: 0 auto;\n" +
                        "        }\n" +
                        "        .header {\n" +
                        "            background-color: #4CAF50;\n" +
                        "            color: white;\n" +
                        "            padding: 20px;\n" +
                        "            text-align: center;\n" +
                        "            border-radius: 5px 5px 0 0;\n" +
                        "        }\n" +
                        "        .content {\n" +
                        "            padding: 20px;\n" +
                        "            background-color: #f9f9f9;\n" +
                        "            border-left: 1px solid #dddddd;\n" +
                        "            border-right: 1px solid #dddddd;\n" +
                        "        }\n" +
                        "        .footer {\n" +
                        "            background-color: #eeeeee;\n" +
                        "            padding: 15px;\n" +
                        "            text-align: center;\n" +
                        "            font-size: 12px;\n" +
                        "            color: #666666;\n" +
                        "            border-radius: 0 0 5px 5px;\n" +
                        "            border: 1px solid #dddddd;\n" +
                        "        }\n" +
                        "        .transaction-details {\n" +
                        "            background-color: #ffffff;\n" +
                        "            border: 1px solid #dddddd;\n" +
                        "            border-radius: 4px;\n" +
                        "            padding: 15px;\n" +
                        "            margin: 20px 0;\n" +
                        "        }\n" +
                        "        .detail-row {\n" +
                        "            display: flex;\n" +
                        "            justify-content: space-between;\n" +
                        "            padding: 8px 0;\n" +
                        "            border-bottom: 1px solid #eeeeee;\n" +
                        "        }\n" +
                        "        .detail-row:last-child {\n" +
                        "            border-bottom: none;\n" +
                        "        }\n" +
                        "        .amount {\n" +
                        "            font-weight: bold;\n" +
                        "            color: #4CAF50;\n" +
                        "        }\n" +
                        "        .balance {\n" +
                        "            font-weight: bold;\n" +
                        "        }\n" +
                        "    </style>\n" +
                        "</head>\n" +
                        "<body>\n" +
                        "    <div class=\"header\">\n" +
                        "        <h1>Transfer Confirmation</h1>\n" +
                        "    </div>\n" +
                        "    <div class=\"content\">\n" +
                        "        <p>Dear %s,</p>\n" +
                        "        <p>We're pleased to confirm your transfer has been processed successfully.</p>\n" +
                        "        \n" +
                        "        <div class=\"transaction-details\">\n" +
                        "            <h3>Transaction Details</h3>\n" +
                        "            <div class=\"detail-row\">\n" +
                        "                <span>Amount:</span>\n" +
                        "                <span class=\"amount\">₦%s</span>\n" +
                        "            </div>\n" +
                        "            <div class=\"detail-row\">\n" +
                        "                <span>Recipient:</span>\n" +
                        "                <span>%s</span>\n" +
                        "            </div>\n" +
                        "            <div class=\"detail-row\">\n" +
                        "                <span>Date:</span>\n" +
                        "                <span>%s</span>\n" +
                        "            </div>\n" +
                        "            <div class=\"detail-row\">\n" +
                        "                <span>Current Balance:</span>\n" +
                        "                <span class=\"balance\">₦%s</span>\n" +
                        "            </div>\n" +
                        "        </div>\n" +
                        "        \n" +
                        "       If you did not authorize this transaction or have any questions, please contact ICT support immediately.</p>\n" +
                        "        \n" +
                        "        <p>Warm regards,<br>The ICT Team</p>\n" +
                        "    </div>\n" +
                        "    <div class=\"footer\">\n" +
                        "        <p>This is an automated message. Please do not reply to this email.</p>\n" +
                        "        <p>&copy; 2025 Evercare Hospital Lekki. All rights reserved.</p>\n" +
                        "    </div>\n" +
                        "</body>\n" +
                        "</html>",
                sender.getName(),
                amount.toString(),
                receiver.getName(),
                formattedDateTime,
                sender.getBalance().toString());

        // Email to receiver
        String receiverSubject = "Cafeteria Transfer Received: ₦" + amount + " Added to Your Account";
        String receiverMessage = String.format(
                "<!DOCTYPE html>\n" +
                        "<html>\n" +
                        "<head>\n" +
                        "    <meta charset=\"UTF-8\">\n" +
                        "    <title>Payment Received</title>\n" +
                        "    <style>\n" +
                        "        body {\n" +
                        "            font-family: Arial, sans-serif;\n" +
                        "            line-height: 1.6;\n" +
                        "            color: #333333;\n" +
                        "            max-width: 600px;\n" +
                        "            margin: 0 auto;\n" +
                        "        }\n" +
                        "        .header {\n" +
                        "            background-color: #2196F3;\n" +
                        "            color: white;\n" +
                        "            padding: 20px;\n" +
                        "            text-align: center;\n" +
                        "            border-radius: 5px 5px 0 0;\n" +
                        "        }\n" +
                        "        .content {\n" +
                        "            padding: 20px;\n" +
                        "            background-color: #f9f9f9;\n" +
                        "            border-left: 1px solid #dddddd;\n" +
                        "            border-right: 1px solid #dddddd;\n" +
                        "        }\n" +
                        "        .footer {\n" +
                        "            background-color: #eeeeee;\n" +
                        "            padding: 15px;\n" +
                        "            text-align: center;\n" +
                        "            font-size: 12px;\n" +
                        "            color: #666666;\n" +
                        "            border-radius: 0 0 5px 5px;\n" +
                        "            border: 1px solid #dddddd;\n" +
                        "        }\n" +
                        "        .transaction-details {\n" +
                        "            background-color: #ffffff;\n" +
                        "            border: 1px solid #dddddd;\n" +
                        "            border-radius: 4px;\n" +
                        "            padding: 15px;\n" +
                        "            margin: 20px 0;\n" +
                        "        }\n" +
                        "        .detail-row {\n" +
                        "            display: flex;\n" +
                        "            justify-content: space-between;\n" +
                        "            padding: 8px 0;\n" +
                        "            border-bottom: 1px solid #eeeeee;\n" +
                        "        }\n" +
                        "        .detail-row:last-child {\n" +
                        "            border-bottom: none;\n" +
                        "        }\n" +
                        "        .amount {\n" +
                        "            font-weight: bold;\n" +
                        "            color: #2196F3;\n" +
                        "        }\n" +
                        "        .balance {\n" +
                        "            font-weight: bold;\n" +
                        "        }\n" +
                        "    </style>\n" +
                        "</head>\n" +
                        "<body>\n" +
                        "    <div class=\"header\">\n" +
                        "        <h1>Payment Received</h1>\n" +
                        "    </div>\n" +
                        "    <div class=\"content\">\n" +
                        "        <p>Dear %s,</p>\n" +
                        "        <p>Great news! You've received a payment to your account.</p>\n" +
                        "        \n" +
                        "        <div class=\"transaction-details\">\n" +
                        "            <h3>Transaction Details</h3>\n" +
                        "            <div class=\"detail-row\">\n" +
                        "                <span>Amount Received:</span>\n" +
                        "                <span class=\"amount\">₦%s</span>\n" +
                        "            </div>\n" +
                        "            <div class=\"detail-row\">\n" +
                        "                <span>From:</span>\n" +
                        "                <span>%s</span>\n" +
                        "            </div>\n" +
                        "            <div class=\"detail-row\">\n" +
                        "                <span>Date:</span>\n" +
                        "                <span>%s</span>\n" +
                        "            </div>\n" +
                        "            <div class=\"detail-row\">\n" +
                        "                <span>Updated Balance:</span>\n" +
                        "                <span class=\"balance\">₦%s</span>\n" +
                        "            </div>\n" +
                        "        </div>\n" +
                        "        \n" +
                        "        <p>The funds are now available in your cafeteria account and ready to use.</p>\n" +
                        "        \n" +
                        "        <p>Best regards,<br>The ICT Team</p>\n" +
                        "    </div>\n" +
                        "    <div class=\"footer\">\n" +
                        "        <p>This is an automated message. Please do not reply to this email.</p>\n" +
                        "        <p>&copy; 2025 Evercare Hospital Lekki. All rights reserved.</p>\n" +
                        "    </div>\n" +
                        "</body>\n" +
                        "</html>",
                receiver.getName(),
                amount.toString(),
                sender.getName(),
                formattedDateTime,
                receiver.getBalance().toString());

        // Send emails asynchronously
        emailService.sendTransferNotification(sender.getEmail(), senderSubject, senderMessage);
        emailService.sendTransferNotification(receiver.getEmail(), receiverSubject, receiverMessage);
    }


    //SERVICE METHOD TO FIND CUSTOMER BY EMAIL
    @Override
    public Customer findByEmail(String email) {
        return customerRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
    }

    @Override
    public DisplayUserNameIdDTO getCustomerBasicInfoByEmail(String email) {
        Customer customer = customerRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        return new DisplayUserNameIdDTO(customer.getName(), customer.getId(), customer.getBalance());
    }




    @Override
    public String getReceiverName(Long receiverId) {
        Customer receiver = customerRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Receiver not found!"));
        return receiver.getName();
    }

    @Override
    @Scheduled(fixedRate = 180000)
    public void autoLockCustomersAfterFiveMinutes() {
        List<Customer> unlockedCustomers = customerRepository.findAllByLockedFalse();
        LocalDateTime now = LocalDateTime.now();

        for (Customer customer : unlockedCustomers) {
            LocalDateTime lastUnlockedAt = customer.getLastActionTime();

            if (lastUnlockedAt != null && lastUnlockedAt.plusMinutes(3).isBefore(now)) {
                customer.setLocked(true);
                customerRepository.save(customer);
            }
        }
    }

    public EmailService getEmailService() {
        return emailService;
    }
}



