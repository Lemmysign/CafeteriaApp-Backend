package Evercare_CafeteriaApp.DTO.PaymentDtoPackage;

import java.time.LocalDateTime;

public class TransactionDTO {
    private Long id;
    private String type; // CREDIT, PURCHASE, TRANSFER
    private double amount;
    private LocalDateTime transactionDate;
    private String description;
    private Long transferToCustomerId;
    private Long customerId; // Instead of embedding Customer object


    public TransactionDTO() {
    }

    public TransactionDTO(Long id, String type, double amount, LocalDateTime transactionDate, String description, Long transferToCustomerId, Long customerId) {
        this.id = id;
        this.type = type;
        this.amount = amount;
        this.transactionDate = transactionDate;
        this.description = description;
        this.transferToCustomerId = transferToCustomerId;
        this.customerId = customerId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getTransferToCustomerId() {
        return transferToCustomerId;
    }

    public void setTransferToCustomerId(Long transferToCustomerId) {
        this.transferToCustomerId = transferToCustomerId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }
}
