package Evercare_CafeteriaApp.Controller;

import Evercare_CafeteriaApp.DTO.OrderDtoPackage.CustomerOrdersResponseDTO;
import Evercare_CafeteriaApp.DTO.OrderDtoPackage.OrderDTO;
import Evercare_CafeteriaApp.Exceptions.CustomerBlockedException;
import Evercare_CafeteriaApp.Exceptions.ResourceNotFoundException;
import Evercare_CafeteriaApp.Model.Customer;
import Evercare_CafeteriaApp.Model.Order;
import Evercare_CafeteriaApp.Model.OrderStatus;
import Evercare_CafeteriaApp.Model.Server;
import Evercare_CafeteriaApp.Repository.CustomerRepoPackage.CustomerRepository;
import Evercare_CafeteriaApp.Repository.OrderRepoPackage.OrderRepository;
import Evercare_CafeteriaApp.Repository.OrderRepoPackage.OrderStatusRepository;
import Evercare_CafeteriaApp.ServiceImplementation.OrderServiceImpl;
import Evercare_CafeteriaApp.ServiceImplementation.ServerServiceImpl;
import Evercare_CafeteriaApp.Services.OrderService;
import Evercare_CafeteriaApp.Services.ServerService;
import jakarta.persistence.Entity;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/servers")
public class ServerRestController {

    private final ServerService serverService;
    private final CustomerRepository customerRepository;
    private final OrderStatusRepository orderStatusRepository;
    private final OrderRepository orderRepository;
    private OrderServiceImpl orderService;


    public ServerRestController(ServerService serverService, CustomerRepository customerRepository, OrderStatusRepository orderStatusRepository, OrderRepository orderRepository) {
        this.serverService = serverService;
        this.customerRepository = customerRepository;
        this.orderStatusRepository = orderStatusRepository;
        this.orderRepository = orderRepository;
    }

    @PostMapping("/serverLogin")
    public ResponseEntity<Map<String, Object>> login(
            @RequestParam String email,
            @RequestParam String password,
            HttpServletRequest request) {
        try {
            // Invalidate previous session
            HttpSession oldSession = request.getSession(false);
            if (oldSession != null) {
                oldSession.invalidate();
            }
            // Authenticate user
            Server server = serverService.login(email, password);
            // Create new session and store user info
            HttpSession newSession = request.getSession(true);
            newSession.setAttribute("serverId", server.getServerId());
            newSession.setAttribute("serverEmail", server.getServerEmail());

            // Prepare response (no need to send session token)
            Map<String, Object> response = new HashMap<>();
            response.put("name", server.getServerName());
            response.put("id", server.getServerId());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }


    @PostMapping("/serverLogout")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate(); // Destroy session
        }

        response.setHeader("Set-Cookie", "JSESSIONID=; Path=/; HttpOnly; Max-Age=0");

        return ResponseEntity.ok("Logged out successfully.");
    }

    //Method to display server details
    @GetMapping("/serverDetails")
    public ResponseEntity<?> getCurrentServer(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("serverEmail") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not logged in");
        }

        String email = (String) session.getAttribute("serverEmail");
        String sessionToken = (String) session.getAttribute("sessionToken");

        try {
            Server server = serverService.findByEmail(email);
            Map<String, Object> response = new HashMap<>();
            response.put("id", server.getServerId());
            response.put("name", server.getServerName());
            response.put("email", server.getServerEmail());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @GetMapping("/customer/{customerId}/orders")
    public ResponseEntity<CustomerOrdersResponseDTO> getCustomerPendingOrders(@PathVariable Long customerId) {
        try {
            // Get customer first to include in response
            Customer customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));

            // Get pending orders (will throw exception if customer is locked/blocked)
            List<OrderDTO> pendingOrders = serverService.getCustomerPendingOrders(customerId);

            // Create response
            CustomerOrdersResponseDTO response = new CustomerOrdersResponseDTO(
                    customer.getId(),
                    customer.getName(),
                    customer.isLocked(),
                    customer.isBlocked(),
                    pendingOrders
            );

            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (CustomerBlockedException e) {
            // Return the customer info but with empty orders list
            Customer customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));

            CustomerOrdersResponseDTO response = new CustomerOrdersResponseDTO(
                    customer.getId(),
                    customer.getName(),
                    customer.isLocked(),
                    customer.isBlocked(),
                    List.of() // Empty list
            );

            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving customer orders", e);
        }
    }


    @PutMapping("/customer/{customerId}/orders/{orderId}/serve")
    public ResponseEntity<OrderDTO> serveCustomerOrder(@PathVariable Long customerId, @PathVariable Long orderId) {
        try {
            OrderDTO servedOrder = serverService.serveCustomerOrder(orderId, customerId);
            return ResponseEntity.ok(servedOrder);
        } catch (ResourceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (CustomerBlockedException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error serving customer order", e);
        }
    }


    public OrderStatusRepository getOrderStatusRepository() {
        return orderStatusRepository;
    }

    public OrderRepository getOrderRepository() {
        return orderRepository;
    }
}
