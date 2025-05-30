package Evercare_CafeteriaApp.DTO.OrderDtoPackage;

import java.util.List;

public class CustomerOrdersResponseDTO {
    private Long customerId;
    private String customerName;
    private boolean isLocked;
    private boolean isBlocked;
    private List<OrderDTO> pendingOrders;

    public CustomerOrdersResponseDTO() {
    }

    public CustomerOrdersResponseDTO(Long customerId, String customerName, boolean isLocked, boolean isBlocked, List<OrderDTO> pendingOrders) {
        this.customerId = customerId;
        this.customerName = customerName;
        this.isLocked = isLocked;
        this.isBlocked = isBlocked;
        this.pendingOrders = pendingOrders;
    }

    // Getters and setters
    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public void setBlocked(boolean blocked) {
        isBlocked = blocked;
    }

    public List<OrderDTO> getPendingOrders() {
        return pendingOrders;
    }

    public void setPendingOrders(List<OrderDTO> pendingOrders) {
        this.pendingOrders = pendingOrders;
    }
}