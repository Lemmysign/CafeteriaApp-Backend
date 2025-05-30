package Evercare_CafeteriaApp.Model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transactionId;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType transactionType;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus transactionStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    @Column(nullable = false)
    private boolean refundStatus;

    @Column(nullable = false)
    private boolean balanceUpdated;

    @Column(nullable = false)
    private LocalDateTime transactionDate;

    // Default constructor
    public Transaction() {}

    // Full constructor
    public Transaction(Long transactionId, TransactionType transactionType, BigDecimal amount, LocalDateTime transactionDate,
                       boolean refundStatus, String description, TransactionStatus transactionStatus,
                       PaymentMethod paymentMethod, Customer customer, boolean balanceUpdated) {
        this.transactionId = transactionId;
        this.transactionType = transactionType;
        this.amount = amount;
        this.transactionDate = transactionDate != null ? transactionDate : LocalDateTime.now();
        this.refundStatus = refundStatus;
        this.description = description;
        this.transactionStatus = transactionStatus;
        this.paymentMethod = paymentMethod;
        this.customer = customer;
        this.balanceUpdated = balanceUpdated;
    }

    // Builder Class
    public static class TransactionBuilder {
        private Long id;
        private TransactionType transactionType;
        private BigDecimal amount;
        private LocalDateTime transactionDate = LocalDateTime.now();
        private boolean refundStatus = false;
        private String description;
        private TransactionStatus transactionStatus;
        private PaymentMethod paymentMethod;
        private Customer customer;
        private boolean balanceUpdated = false;

        public TransactionBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public TransactionBuilder type(TransactionType transactionType) {
            this.transactionType = transactionType;
            return this;
        }

        public TransactionBuilder amount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public TransactionBuilder transactionDate(LocalDateTime transactionDate) {
            this.transactionDate = transactionDate;
            return this;
        }

        public TransactionBuilder refundStatus(boolean refundStatus) {
            this.refundStatus = refundStatus;
            return this;
        }

        public TransactionBuilder description(String description) {
            this.description = description;
            return this;
        }

        public TransactionBuilder transactionStatus(TransactionStatus transactionStatus) {
            this.transactionStatus = transactionStatus;
            return this;
        }

        public TransactionBuilder paymentMethod(PaymentMethod paymentMethod) {
            this.paymentMethod = paymentMethod;
            return this;
        }

        public TransactionBuilder customer(Customer customer) {
            this.customer = customer;
            return this;
        }

        public TransactionBuilder balanceUpdated(boolean balanceUpdated) {
            this.balanceUpdated = balanceUpdated;
            return this;
        }

        public Transaction build() {
            return new Transaction(id, transactionType, amount, transactionDate, refundStatus,
                    description, transactionStatus, paymentMethod, customer, balanceUpdated);
        }
    }

    // Getters and Setters
    public Long getTransactionId() { return transactionId; }
    public void setTransactionId(Long transactionId) { this.transactionId = transactionId; }

    public TransactionType getTransactionType() { return transactionType; }
    public void setTransactionType(TransactionType transactionType) { this.transactionType = transactionType; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public LocalDateTime getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDateTime transactionDate) { this.transactionDate = transactionDate; }

    public boolean isRefundStatus() { return refundStatus; }
    public void setRefundStatus(boolean refundStatus) { this.refundStatus = refundStatus; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    public TransactionStatus getTransactionStatus() { return transactionStatus; }
    public void setTransactionStatus(TransactionStatus transactionStatus) { this.transactionStatus = transactionStatus; }

    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }

    public boolean isBalanceUpdated() { return balanceUpdated; }
    public void setBalanceUpdated(boolean balanceUpdated) { this.balanceUpdated = balanceUpdated; }

    // Equals and HashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Transaction that)) return false;
        return refundStatus == that.refundStatus &&
                balanceUpdated == that.balanceUpdated &&
                Objects.equals(transactionId, that.transactionId) &&
                transactionType == that.transactionType &&
                Objects.equals(amount, that.amount) &&
                Objects.equals(transactionDate, that.transactionDate) &&
                transactionStatus == that.transactionStatus &&
                paymentMethod == that.paymentMethod &&
                Objects.equals(description, that.description) &&
                Objects.equals(customer, that.customer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionId, transactionType, amount, transactionDate,
                transactionStatus, paymentMethod, refundStatus, balanceUpdated, description, customer);
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "transactionId=" + transactionId +
                ", transactionType=" + transactionType +
                ", amount=" + amount +
                ", transactionStatus=" + transactionStatus +
                ", paymentMethod=" + paymentMethod +
                ", transactionDate=" + transactionDate +
                ", refundStatus=" + refundStatus +
                ", balanceUpdated=" + balanceUpdated +
                ", description='" + description + '\'' +
                ", customer=" + customer +
                '}';
    }
}
