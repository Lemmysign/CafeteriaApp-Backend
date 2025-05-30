package Evercare_CafeteriaApp.Services;

import Evercare_CafeteriaApp.DTO.OrderDtoPackage.OrderDTO;
import Evercare_CafeteriaApp.DTO.OrderDtoPackage.OrderItemDTO;
import Evercare_CafeteriaApp.Model.Customer;
import Evercare_CafeteriaApp.Model.Order;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface OrderService {
    Order createOrder(Long customerId, List<OrderItemDTO> orderItemDTOs);
    Order attemptToCreateOrder(Long customerId, List<OrderItemDTO> orderItemDTOs);

    BigDecimal calculatePendingOrdersAmount(Customer customer);

    Order cancelOrder(Long orderId, Long customerId);


    List<OrderDTO> getCustomerOrders(Long customerId);
    void cleanupCancelledOrders();

    Long getTotalOrdersToday();
    Long getTotalServedOrdersToday();




}
