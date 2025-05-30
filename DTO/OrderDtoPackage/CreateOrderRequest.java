package Evercare_CafeteriaApp.DTO.OrderDtoPackage;

import java.util.List;

public class CreateOrderRequest {
    private Long customerId;
    private List<OrderItemDTO> orderItems;

    // Getters and setters
    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public List<OrderItemDTO> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItemDTO> orderItems) {
        this.orderItems = orderItems;
    }
}