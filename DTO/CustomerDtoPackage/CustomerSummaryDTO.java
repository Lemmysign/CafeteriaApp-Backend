package Evercare_CafeteriaApp.DTO.CustomerDtoPackage;

import java.math.BigDecimal;

public class CustomerSummaryDTO {
    private Long id;
    private String name;
    private BigDecimal balance;
    private boolean blocked;

    public CustomerSummaryDTO() {
    }

    public CustomerSummaryDTO(Long id, String name, BigDecimal balance, boolean blocked) {
        this.id = id;
        this.name = name;
        this.balance = balance;
        this.blocked = blocked;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    // Constructor, getters, setters
}