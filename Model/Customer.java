package Evercare_CafeteriaApp.Model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "customers")
public class Customer {


    @Id
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private boolean verified;

    @Version
    private Long version;

    private LocalDateTime lastActionTime;

    private BigDecimal balance;
    private boolean isLocked;
    private boolean isBlocked;
    private boolean hideBalance;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @OneToOne(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private EmailVerificationToken emailVerificationToken;

    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    private Set<Order> orders = new HashSet<>();

    @OneToMany(mappedBy = "customer", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    private Set<Transaction> transactions = new HashSet<>();


    public Customer() {
        this.createdDate = LocalDateTime.now(); // Auto-set creation date
        this.lastActionTime = LocalDateTime.now(); // Auto-set initial last action time
    }

    public Customer(Long id, Role role, String name, String email, String password, String phone,
                    BigDecimal balance, boolean isLocked, boolean isBlocked, boolean hideBalance,
                    boolean verified, LocalDateTime createdDate) {
        this.id = id; // User-provided ID
        this.role = role;
        this.name = name;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.balance = balance;
        this.isLocked = isLocked;
        this.isBlocked = isBlocked;
        this.hideBalance = hideBalance;
        this.verified = verified;
        this.createdDate = (createdDate != null) ? createdDate : LocalDateTime.now();
        this.lastActionTime = LocalDateTime.now(); // Set initial last action time
    }

    public static class CustomerBuilder {
        private Long id; // Changed from Long to String
        private Role role;
        private String name;
        private String email;
        private String password;
        private String phone;
        private BigDecimal balance;
        private boolean isLocked;
        private boolean isBlocked;
        private boolean hideBalance;
        private boolean verified;
        private LocalDateTime createdDate;
        private LocalDateTime lastActionTime;
        private Set<Order> orders = new HashSet<>();
        private Set<Transaction> transactions = new HashSet<>();

        public CustomerBuilder id(Long id) { // Changed from Long to String
            this.id = id;
            return this;
        }

        public CustomerBuilder role(Role role) {
            this.role = role;
            return this;
        }

        public CustomerBuilder name(String name) {
            this.name = name;
            return this;
        }

        public CustomerBuilder email(String email) {
            this.email = email;
            return this;
        }

        public CustomerBuilder password(String password) {
            this.password = password;
            return this;
        }

        public CustomerBuilder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public CustomerBuilder balance(BigDecimal balance) {
            this.balance = balance;
            return this;
        }

        public CustomerBuilder locked(boolean isLocked) {
            this.isLocked = isLocked;
            return this;
        }

        public CustomerBuilder blocked(boolean isBlocked) {
            this.isBlocked = isBlocked;
            return this;
        }

        public CustomerBuilder hideBalance(boolean hideBalance) {
            this.hideBalance = hideBalance;
            return this;
        }

        public CustomerBuilder verified(boolean verified) {
            this.verified = verified;
            return this;
        }

        public CustomerBuilder createdDate(LocalDateTime createdDate) {
            this.createdDate = createdDate;
            return this;
        }

        public CustomerBuilder lastActionTime(LocalDateTime lastActionTime) {
            this.lastActionTime = lastActionTime;
            return this;
        }

        public CustomerBuilder orders(Set<Order> orders) {
            this.orders = orders;
            return this;
        }

        public CustomerBuilder transactions(Set<Transaction> transactions) {
            this.transactions = transactions;
            return this;
        }

        public Customer build() {
            Customer customer = new Customer(id, role, name, email, password, phone, balance, isLocked, isBlocked, hideBalance, verified, createdDate);
            if (lastActionTime != null) {
                customer.setLastActionTime(lastActionTime);
            }
            return customer;
        }

        public Set<Order> getOrders() {
            return orders;
        }

        public void setOrders(Set<Order> orders) {
            this.orders = orders;
        }

        public Set<Transaction> getTransactions() {
            return transactions;
        }

        public void setTransactions(Set<Transaction> transactions) {
            this.transactions = transactions;
        }
    }

    // Getters and Setters
    public Long getId() { return id; } // Changed from Long to String
    public void setId(Long id) { this.id = id; } // Changed from Long to String

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public boolean isLocked() { return isLocked; }
    public void setLocked(boolean isLocked) { this.isLocked = isLocked; }

    public boolean isBlocked() { return isBlocked; }
    public void setBlocked(boolean isBlocked) { this.isBlocked = isBlocked; }

    public boolean isHideBalance() { return hideBalance; }
    public void setHideBalance(boolean hideBalance) { this.hideBalance = hideBalance; }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public boolean isVerified() { return !verified; }
    public void setVerified(boolean verified) { this.verified = verified; }

    public LocalDateTime getLastActionTime() { return lastActionTime; }
    public void setLastActionTime(LocalDateTime lastActionTime) { this.lastActionTime = lastActionTime; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public Set<Order> getOrders() { return orders; }
    public void setOrders(Set<Order> orders) { this.orders = orders; }

    public Set<Transaction> getTransactions() { return transactions; }
    public void setTransactions(Set<Transaction> transactions) { this.transactions = transactions; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Customer customer = (Customer) o;
        return isLocked == customer.isLocked &&
                isBlocked == customer.isBlocked &&
                hideBalance == customer.hideBalance &&
                verified == customer.verified &&
                Objects.equals(id, customer.id) &&
                Objects.equals(role, customer.role) &&
                Objects.equals(name, customer.name) &&
                Objects.equals(email, customer.email) &&
                Objects.equals(password, customer.password) &&
                Objects.equals(phone, customer.phone) &&
                Objects.equals(balance, customer.balance) &&
                Objects.equals(lastActionTime, customer.lastActionTime) &&
                Objects.equals(createdDate, customer.createdDate) &&
                Objects.equals(orders, customer.orders) &&
                Objects.equals(transactions, customer.transactions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, role, name, email, password, phone, balance, isLocked, isBlocked, hideBalance, verified, lastActionTime, createdDate, orders, transactions);
    }

    @Override
    public String toString() {
        return "Customer{" +
                "id='" + id + '\'' +
                ", role=" + role +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", phone='" + phone + '\'' +
                ", balance=" + balance +
                ", isLocked=" + isLocked +
                ", isBlocked=" + isBlocked +
                ", hideBalance=" + hideBalance +
                ", verified=" + verified +
                ", lastActionTime=" + lastActionTime +
                ", createdDate=" + createdDate +
                ", orders=" + orders +
                ", transactions=" + transactions +
                '}';
    }

    public EmailVerificationToken getEmailVerificationToken() {
        return emailVerificationToken;
    }

    public void setEmailVerificationToken(EmailVerificationToken emailVerificationToken) {
        this.emailVerificationToken = emailVerificationToken;
    }
}