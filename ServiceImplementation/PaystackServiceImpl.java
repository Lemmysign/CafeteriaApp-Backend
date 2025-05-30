package Evercare_CafeteriaApp.ServiceImplementation;
import Evercare_CafeteriaApp.Config.PaystackConfig;
import Evercare_CafeteriaApp.DTO.PaymentDtoPackage.PaystackDTO;
import Evercare_CafeteriaApp.Model.*;
import Evercare_CafeteriaApp.Repository.CustomerRepoPackage.CustomerRepository;
import Evercare_CafeteriaApp.Repository.PaymentRepoPackage.PaymentTransactionRepository;
import Evercare_CafeteriaApp.Services.EmailService;
import Evercare_CafeteriaApp.Services.PaystackService;
import Evercare_CafeteriaApp.Services.TransactionService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("ALL")
@Service
public class PaystackServiceImpl implements PaystackService {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private RestTemplate paystackRestTemplate;

    @Autowired
    private PaystackConfig paystackConfig;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PaymentTransactionRepository paymentTransactionRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private TransactionService transactionService;

    // Fee rates
    private static final BigDecimal FEE_DIVISOR_BELOW_THRESHOLD = new BigDecimal("0.985");
    private static final BigDecimal FEE_DIVISOR_ABOVE_THRESHOLD = new BigDecimal("0.983");
    private static final BigDecimal THRESHOLD_AMOUNT = new BigDecimal("15000");

    //update to backend url
    @Value("${backend.callback.url:https://cafeteriadash.netlify.app/api/payments/callback}")
    private String backendCallbackUrl;

    @Override
    @Transactional
    public PaystackDTO.InitializeTransactionResponse initializeTransaction(PaystackDTO.PaymentRequest paymentRequest, Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        String reference = "EVC_" + UUID.randomUUID().toString().substring(0, 8);

        // Original amount the user wants to add to their wallet
        BigDecimal originalAmount = paymentRequest.getAmount();

        // Calculate the amount with fees for Paystack initialization
        BigDecimal paymentAmountWithFees = calculateAmountWithFees(originalAmount);

        PaymentTransaction paymentTransaction = new PaymentTransaction();
        paymentTransaction.setReference(reference);
        paymentTransaction.setAmount(originalAmount); // Store the original amount
        paymentTransaction.setEmail(customer.getEmail());
        paymentTransaction.setStatus(PaymentTransaction.PaymentStatus.PENDING);
        paymentTransaction.setCustomer(customer);
        // Store the fee amount for reference
        paymentTransaction.setFeeAmount(paymentAmountWithFees.subtract(originalAmount));
        paymentTransaction.setTotalAmount(paymentAmountWithFees);
        paymentTransactionRepository.save(paymentTransaction);

        PaystackDTO.InitializeTransactionRequest request = new PaystackDTO.InitializeTransactionRequest();
        request.setAmount(paymentAmountWithFees.multiply(new BigDecimal("100"))); // Convert to kobo
        request.setEmail(customer.getEmail());
        request.setReference(reference);
        request.setCallbackUrl(backendCallbackUrl); // Use backend for callback
        request.setCurrency("NGN");

        if (paymentRequest.getPaymentChannel() != null && !paymentRequest.getPaymentChannel().isEmpty()) {
            request.setChannels(paymentRequest.getPaymentChannel());
        }

        try {
            String url = paystackConfig.getPaystackBaseUrl() + "/transaction/initialize";
            HttpEntity<PaystackDTO.InitializeTransactionRequest> entity = new HttpEntity<>(request);
            ResponseEntity<PaystackDTO.InitializeTransactionResponse> response =
                    paystackRestTemplate.exchange(url, HttpMethod.POST, entity,
                            PaystackDTO.InitializeTransactionResponse.class);
            return response.getBody();
        } catch (Exception e) {
            // Log the error
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize Paystack transaction: " + e.getMessage(), e);
        }
    }


    // Calculate the amount with fees applied
    //@return The amount with fees that should be charged by Paystack

    private BigDecimal calculateAmountWithFees(BigDecimal originalAmount) {
        BigDecimal divisor;

        if (originalAmount.compareTo(THRESHOLD_AMOUNT) < 0) {
            divisor = FEE_DIVISOR_BELOW_THRESHOLD;
        } else {
            divisor = FEE_DIVISOR_ABOVE_THRESHOLD;
        }

        // Calculate amount with fees: originalAmount / feeRate
        return originalAmount.divide(divisor, 2, RoundingMode.HALF_UP);
    }

    @Override
    @Transactional
    public boolean verifyTransaction(String reference) {
        try {
            String url = paystackConfig.getPaystackBaseUrl() + "/transaction/verify/" + reference;
            ResponseEntity<PaystackDTO.VerifyTransactionResponse> responseEntity =
                    paystackRestTemplate.exchange(url, HttpMethod.GET, HttpEntity.EMPTY,
                            PaystackDTO.VerifyTransactionResponse.class);

            PaystackDTO.VerifyTransactionResponse response = responseEntity.getBody();

            if (response == null || !response.isStatus()) {
                return false;
            }

            String transactionStatus = response.getData().getStatus();
            if ("success".equalsIgnoreCase(transactionStatus)) {
                return updateCustomerBalanceAndTransaction(reference, response);
            }

            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    @Override
    @Transactional
    public boolean processWebhookEvent(PaystackDTO.WebhookEventData webhookEvent) {
        if (webhookEvent == null || !"charge.success".equals(webhookEvent.getEvent())) {
            return false;
        }
        String reference = webhookEvent.getData().getReference();
        // Check if transaction exists without locking
        Optional<PaymentTransaction> optionalPaymentTx = paymentTransactionRepository.findByReference(reference);
        if (optionalPaymentTx.isEmpty()) {
            return false;
        }

        try {
            String url = paystackConfig.getPaystackBaseUrl() + "/transaction/verify/" + reference;
            ResponseEntity<PaystackDTO.VerifyTransactionResponse> responseEntity =
                    paystackRestTemplate.exchange(url, HttpMethod.GET, HttpEntity.EMPTY,
                            PaystackDTO.VerifyTransactionResponse.class);

            PaystackDTO.VerifyTransactionResponse response = responseEntity.getBody();

            if (response == null || !response.isStatus() || !"success".equalsIgnoreCase(response.getData().getStatus())) {
                return false;
            }
            return updateCustomerBalanceAndTransaction(reference, response);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    @Override
    @Transactional
    @Retryable(value = {OptimisticLockingFailureException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 200))
    public boolean updateCustomerBalanceAndTransaction(String reference, PaystackDTO.VerifyTransactionResponse response) {
        // Use pessimistic locking with a query to prevent concurrent updates
        PaymentTransaction paymentTx = entityManager.createQuery(
                        "SELECT pt FROM PaymentTransaction pt WHERE pt.reference = :reference",
                        PaymentTransaction.class)
                .setParameter("reference", reference)
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .getSingleResult();

        if (paymentTx == null) {
            return false;
        }

        // Double-check status after obtaining lock
        if (PaymentTransaction.PaymentStatus.COMPLETED.equals(paymentTx.getStatus())) {
            return true; // Already processed, prevent duplicate processing
        }

        Customer customer = entityManager.find(Customer.class, paymentTx.getCustomer().getId(), LockModeType.PESSIMISTIC_WRITE);
        if (customer == null) {
            return false;
        }

        // Safely extract payment method
        String paymentMethodStr = response.getData().getPaymentMethod();
        if (paymentMethodStr == null) {
            paymentMethodStr = "unknown";
        }

        // Determine TransactionType
        TransactionType paymentTransactionType = resolveTransactionType(paymentMethodStr);

        // Update payment transaction
        paymentTx.setStatus(PaymentTransaction.PaymentStatus.COMPLETED);
        paymentTx.setCompletedAt(LocalDateTime.now());
        paymentTx.setTransactionType(paymentTransactionType);
        entityManager.merge(paymentTx);

        // Update customer balance with ORIGINAL amount, not the amount with fees
        BigDecimal originalAmount = paymentTx.getAmount();
        BigDecimal currentBalance = customer.getBalance() != null ? customer.getBalance() : BigDecimal.ZERO;
        customer.setBalance(currentBalance.add(originalAmount));
        entityManager.merge(customer);

        // Create transaction record
        Transaction transaction = new Transaction();
        transaction.setTransactionType(TransactionType.DEPOSIT);
        transaction.setAmount(originalAmount); // Use original amount
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setRefundStatus(false);
        transaction.setDescription("Fund wallet via Paystack - Ref: " + reference);
        transaction.setTransactionStatus(TransactionStatus.COMPLETED);
        transaction.setCustomer(customer);
        transaction.setBalanceUpdated(true);
        transaction.setPaymentMethod(PaymentMethod.PAYSTACK);
        transactionService.saveTransaction(transaction);

        // Ensure changes are flushed to the database
        entityManager.flush();

        // Send transaction success email
        emailService.sendTransactionSuccessEmail(customer.getEmail(), reference, originalAmount.toString());

        return true;
    }

    // Helper method to map string to TransactionType
    private TransactionType resolveTransactionType(String paymentMethodStr) {
        String method = paymentMethodStr.toLowerCase();
        if (method.contains("card")) {
            return TransactionType.CARD;
        } else if (method.contains("transfer")) {
            return TransactionType.TRANSFER;
        } else if (method.contains("bank")) {
            return TransactionType.TRANSFER;
        } else if (method.contains("ussd")) {
            return TransactionType.TRANSFER;
        } else {
            return TransactionType.DEPOSIT;
        }
    }
}