package Evercare_CafeteriaApp.Model;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "order_status")
public class OrderStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String statusType;

    // Default constructor
    public OrderStatus() {
    }

    // All-args constructor
    public OrderStatus(Long id, String statusType) {
        this.id = id;
        this.statusType = statusType;
    }

    // Builder pattern
    public static class OrderStatusBuilder {
        private Long id;
        private String statusType;

        public OrderStatusBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public OrderStatusBuilder statusType(String statusType) {
            this.statusType = statusType;
            return this;
        }

        public OrderStatus build() {
            return new OrderStatus(id, statusType);
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStatusType() {
        return statusType;
    }

    public void setStatusType(String statusType) {
        this.statusType = statusType;
    }

    // equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderStatus that)) return false;
        return Objects.equals(id, that.id) &&
                Objects.equals(statusType, that.statusType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, statusType);
    }

    // toString
    @Override
    public String toString() {
        return "OrderStatus{" +
                "id=" + id +
                ", statusType='" + statusType + '\'' +
                '}';
    }
}
