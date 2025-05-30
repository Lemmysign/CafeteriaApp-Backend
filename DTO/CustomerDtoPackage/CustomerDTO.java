package Evercare_CafeteriaApp.DTO.CustomerDtoPackage;

import java.time.LocalDateTime;

public class CustomerDTO {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private double balance;
    private boolean isLocked;
    private boolean isBlocked;
    private boolean hideBalance;
    private Long version;
    private boolean verified;
    private  LocalDateTime lastActionTime;
    private LocalDateTime createdDate;
    private String role;

    public CustomerDTO() {
    }

    public CustomerDTO(Long id, String name, String email, String phone, double balance,
                       boolean isLocked, boolean isBlocked, boolean hideBalance, Long version,
                       boolean verified, LocalDateTime lastActionTime, LocalDateTime createdDate, String role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.balance = balance;
        this.isLocked = isLocked;
        this.isBlocked = isBlocked;
        this.hideBalance = hideBalance;
        this.version = version;
        this.verified = verified;
        this.lastActionTime = lastActionTime;  // Added this line
        this.createdDate = createdDate;
        this.role = role;
    }

    public CustomerDTO(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }

    public boolean isLocked() { return isLocked; }
    public void setLocked(boolean locked) { isLocked = locked; }

    public boolean isBlocked() { return isBlocked; }
    public void setBlocked(boolean blocked) { isBlocked = blocked; }

    public boolean isHideBalance() { return hideBalance; }
    public void setHideBalance(boolean hideBalance) { this.hideBalance = hideBalance; }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }

    // Add this getter method
    public LocalDateTime getLastActionTime() {
        return lastActionTime;
    }

    // Add this setter method
    public void setLastActionTime(LocalDateTime lastActionTime) {
        this.lastActionTime = lastActionTime;
    }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
