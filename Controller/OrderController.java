package Evercare_CafeteriaApp.Controller;

import Evercare_CafeteriaApp.DTO.OrderDtoPackage.CreateOrderRequest;
import Evercare_CafeteriaApp.DTO.OrderDtoPackage.OrderDTO;
import Evercare_CafeteriaApp.Exceptions.CustomerBlockedException;
import Evercare_CafeteriaApp.Exceptions.InsufficientFundsException;
import Evercare_CafeteriaApp.Exceptions.ResourceNotFoundException;
import Evercare_CafeteriaApp.Model.Order;
import Evercare_CafeteriaApp.Services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createOrder(@RequestBody CreateOrderRequest request, HttpServletRequest servletRequest) {
        try {
            // Get customer ID from session
            HttpSession session = servletRequest.getSession(false);
            if (session == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "You must be logged in to place an order"));
            }

            Long customerId = (Long) session.getAttribute("customerId");
            if (customerId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "You must be logged in to place an order"));
            }

            // Create order
            Order createdOrder = orderService.createOrder(customerId, request.getOrderItems());

            // Convert to response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("orderId", createdOrder.getOrderId());
            response.put("message", "Order placed successfully");

            return ResponseEntity.ok(response);

        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (CustomerBlockedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        } catch (InsufficientFundsException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create order: " + e.getMessage()));
        }
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Long orderId, HttpServletRequest servletRequest) {
        try {
            // Get customer ID from session
            HttpSession session = servletRequest.getSession(false);
            if (session == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "You must be logged in to cancel an order"));
            }

            Long customerId = (Long) session.getAttribute("customerId");
            if (customerId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "You must be logged in to cancel an order"));
            }

            // Cancel order
            Order cancelledOrder = orderService.cancelOrder(orderId, customerId);

            // Convert to response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("orderId", cancelledOrder.getOrderId());
            response.put("message", "Order cancelled successfully");

            return ResponseEntity.ok(response);

        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to cancel order: " + e.getMessage()));
        }
    }

    @GetMapping("/my-orders")
    public ResponseEntity<?> getMyOrders(HttpServletRequest servletRequest) {
        try {
            // Get customer ID from session
            HttpSession session = servletRequest.getSession(false);
            if (session == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "You must be logged in to view your orders"));
            }

            Long customerId = (Long) session.getAttribute("customerId");
            if (customerId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "You must be logged in to view your orders"));
            }

            // Get customer orders
            List<OrderDTO> orders = orderService.getCustomerOrders(customerId);

            return ResponseEntity.ok(orders);

        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get orders: " + e.getMessage()));
        }
    }

    @GetMapping("/total-today")
    public Long getTotalOrdersToday() {
        return orderService.getTotalOrdersToday();
    }

    @GetMapping("/served-total-today")
    public Long getTotalServedOrdersToday() {
        return orderService.getTotalServedOrdersToday();
    }
}