package Evercare_CafeteriaApp.ServiceImplementation;

import Evercare_CafeteriaApp.DTO.OrderDtoPackage.OrderDTO;
import Evercare_CafeteriaApp.DTO.ServerDtoPackage.DisplayServerNameIdDTO;
import Evercare_CafeteriaApp.Exceptions.CustomerBlockedException;
import Evercare_CafeteriaApp.Exceptions.ResourceNotFoundException;
import Evercare_CafeteriaApp.Mapper.CustomerMapper;
import Evercare_CafeteriaApp.Mapper.ServerMapper;
import Evercare_CafeteriaApp.Model.*;
import Evercare_CafeteriaApp.Repository.CustomerRepoPackage.CustomerRepository;
import Evercare_CafeteriaApp.Repository.OrderRepoPackage.OrderRepository;
import Evercare_CafeteriaApp.Repository.OrderRepoPackage.OrderStatusRepository;
import Evercare_CafeteriaApp.Repository.RoleRepository;
import Evercare_CafeteriaApp.Repository.ServiceRepoPackage.ServerRepository;
import Evercare_CafeteriaApp.Services.ServerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class ServerServiceImpl implements ServerService {

    private final ServerRepository serverRepository;

    private final CustomerRepository customerRepository;

    private final CustomerMapper customerMapper;

    private final ServerMapper serverMapper;

    private final PasswordEncoder passwordEncoder;

    private final RoleRepository roleRepository;

    private final OrderStatusRepository orderStatusRepository;
    private final OrderRepository orderRepository;
    private final OrderServiceImpl orderService;
    private final Logger logger = LoggerFactory.getLogger(ServerServiceImpl.class);

    @Autowired
    public ServerServiceImpl(ServerRepository serverRepository, CustomerRepository customerRepository, CustomerMapper customerMapper, ServerMapper serverMapper, PasswordEncoder passwordEncoder, RoleRepository roleRepository, OrderStatusRepository orderStatusRepository, OrderRepository orderRepository, OrderServiceImpl orderService) {
        this.serverRepository = serverRepository;
        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
        this.serverMapper = serverMapper;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.orderStatusRepository = orderStatusRepository;
        this.orderRepository = orderRepository;
        this.orderService = orderService;
    }

    @Override
    public Server login(String email, String password) {
        Server server = serverRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("Invalid email or password!"));

        Role serverRole = roleRepository.findByRoleName("SERVER")
                .orElseThrow(() -> new RuntimeException("Default role SERVER not found!"));



        // First-time login: If the password is stored in plaintext, hash it before checking
        if (!server.getServerPassword().startsWith("$2a$")) {
            if (server.getServerPassword().equals(password)) {
                server.setServerPassword(passwordEncoder.encode(password));
                serverRepository.save(server);
            } else {
                throw new SecurityException("Invalid email or password!");
            }
        }

        if (!passwordEncoder.matches(password, server.getServerPassword())) {

            if (server.getServerPassword().equals(password)) {
                // Hash and update the stored password
                server.setServerPassword(passwordEncoder.encode(password));
                serverRepository.save(server);
            } else {
                throw new RuntimeException("Invalid email or password!");
            }
        }

        if (Boolean.TRUE.equals(server.isServerBlocked())) {
            throw new RuntimeException("Account is Blocked. Please contact support.");
        }
        serverMapper.toDTO(server);
        return server;
    }


    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false); // Get current session if exists
        if (session != null) {
            session.invalidate(); // Invalidate the session
        }
    }


    @Override
    public DisplayServerNameIdDTO getCustomerBasicInfoByEmail(String email) {
        Server server = serverRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("Server not found"));
        return new DisplayServerNameIdDTO(server.getServerName(), server.getServerId());

    }


    @Override
    public Server findByEmail(String email) {
        return serverRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("Server not found"));
    }

    @Override
  @org.springframework.transaction.annotation.Transactional
    public List<OrderDTO> getCustomerPendingOrders(Long customerId) {
        // Validate customer exists
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));


        // Get pending order status
        OrderStatus pendingStatus = orderStatusRepository.findByStatusType("PENDING")
                .orElseThrow(() -> new ResourceNotFoundException("Pending order status not found"));

        // Get customer pending orders
        List<Order> pendingOrders = orderRepository.findByCustomerAndOrderStatusWithItems(customer, pendingStatus);

        // Convert to DTOs
        return pendingOrders.stream()
                .map(orderService::convertToOrderDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrderDTO serveCustomerOrder(Long orderId, Long customerId) {
        // Validate customer exists
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));

        // Check if customer account is locked or blocked
        if (customer.isLocked()) {
            throw new CustomerBlockedException("Customer account is locked and cannot be served");
        }

        if (customer.isBlocked()) {
            throw new CustomerBlockedException("Customer account is blocked and cannot be served");
        }

        // Validate order exists and belongs to customer
        Order order = orderRepository.findByOrderIdAndCustomerId(orderId, customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId + " for customer id: " + customerId));

        // Check if order is in PENDING status
        if (!"PENDING".equals(order.getOrderStatus().getStatusType())) {
            throw new IllegalArgumentException("Only pending orders can be served. Current status: " + order.getOrderStatus().getStatusType());
        }

        // Get served status
        OrderStatus servedStatus = orderStatusRepository.findByStatusType("SERVED")
                .orElseThrow(() -> new ResourceNotFoundException("Served order status not found"));

        // Update order status
        order.setOrderStatus(servedStatus);
        order.setServedAt(LocalDateTime.now());

        // Update customer's last action time
        customer.setLastActionTime(LocalDateTime.now());
        customerRepository.save(customer);

        // Save updated order
        order = orderRepository.save(order);

        logger.info("Order ID: {} for Customer ID: {} has been served", orderId, customerId);

        return orderService.convertToOrderDTO(order);
    }

    public CustomerRepository getCustomerRepository() {
        return customerRepository;
    }

    public CustomerMapper getCustomerMapper() {
        return customerMapper;
    }

    public OrderServiceImpl getOrderService() {
        return orderService;
    }





}
