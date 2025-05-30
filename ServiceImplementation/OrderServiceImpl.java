package Evercare_CafeteriaApp.ServiceImplementation;

import Evercare_CafeteriaApp.DTO.OrderDtoPackage.OrderDTO;
import Evercare_CafeteriaApp.DTO.OrderDtoPackage.OrderItemDTO;
import Evercare_CafeteriaApp.Exceptions.CustomerBlockedException;
import Evercare_CafeteriaApp.Exceptions.InsufficientFundsException;
import Evercare_CafeteriaApp.Exceptions.ResourceNotFoundException;
import Evercare_CafeteriaApp.Model.*;
import Evercare_CafeteriaApp.Repository.CustomerRepoPackage.CustomerRepository;
import Evercare_CafeteriaApp.Repository.MenuRepoPackage.MenuItemRepository;
import Evercare_CafeteriaApp.Repository.OrderRepoPackage.OrderItemRepository;
import Evercare_CafeteriaApp.Repository.OrderRepoPackage.OrderRepository;
import Evercare_CafeteriaApp.Repository.OrderRepoPackage.OrderStatusRepository;
import Evercare_CafeteriaApp.Services.OrderService;
import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final MenuItemRepository menuItemRepository;
    private final OrderStatusRepository orderStatusRepository;
    private final OrderItemRepository orderItemRepository;
    private final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository,
                            CustomerRepository customerRepository,
                            MenuItemRepository menuItemRepository,
                            OrderStatusRepository orderStatusRepository,
                            OrderItemRepository orderItemRepository) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.menuItemRepository = menuItemRepository;
        this.orderStatusRepository = orderStatusRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @Override
    @Transactional
    public Order createOrder(Long customerId, List<OrderItemDTO> orderItemDTOs) {
        int maxAttempts = 3;
        int attempt = 0;

        while (true) {
            try {
                return attemptToCreateOrder(customerId, orderItemDTOs);
            } catch (OptimisticLockException e) {
                if (++attempt >= maxAttempts) {
                    throw new IllegalStateException("Unable to place order due to concurrent activity. Please try again.");
                }
            }
        }
    }



    @Transactional
    public Order attemptToCreateOrder(Long customerId, List<OrderItemDTO> orderItemDTOs) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));

        if (customer.isBlocked()) {
            throw new CustomerBlockedException("Customer is blocked and cannot place orders");
        }

        OrderStatus pendingStatus = orderStatusRepository.findByStatusType("PENDING")
                .orElseThrow(() -> new ResourceNotFoundException("Pending order status not found"));

        BigDecimal totalAmount = BigDecimal.ZERO;


        List<Long> itemIds = orderItemDTOs.stream()
                .map(OrderItemDTO::getMenuItemId)
                .collect(Collectors.toList());

        List<MenuItem> menuItems = menuItemRepository.findAllById(itemIds);

        Map<Long, MenuItem> menuItemMap = menuItems.stream()
                .collect(Collectors.toMap(MenuItem::getId, Function.identity()));

        for (OrderItemDTO itemDTO : orderItemDTOs) {
            MenuItem menuItem = menuItemMap.get(itemDTO.getMenuItemId());

            if (menuItem == null) {
                throw new ResourceNotFoundException("Menu item not found with id: " + itemDTO.getMenuItemId());
            }

            if (!menuItem.isAvailable()) {
                throw new IllegalArgumentException("Menu item is not available: " + menuItem.getName());
            }

            BigDecimal itemSubtotal = menuItem.getPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity()));
            totalAmount = totalAmount.add(itemSubtotal);
        }


        if (customer.getBalance() == null || customer.getBalance().compareTo(totalAmount) < 0) {
            throw new InsufficientFundsException("Insufficient funds for this order");
        }

        // Deduct balance — optimistic locking will fail if customer is concurrently modified
        customer.setBalance(customer.getBalance().subtract(totalAmount));
        customerRepository.save(customer);

        logger.info("Customer ID: {} - Balance deducted for order. New balance: {}", customerId, customer.getBalance());

        Order order = new Order.OrderBuilder()
                .customer(customer)
                .orderDate(LocalDateTime.now())
                .orderStatus(pendingStatus)
                .totalAmount(totalAmount)
                .build();

        order = orderRepository.save(order);

        Set<OrderItem> orderItems = new HashSet<>();
        for (OrderItemDTO itemDTO : orderItemDTOs) {
            MenuItem menuItem = menuItemMap.get(itemDTO.getMenuItemId());

            OrderItem orderItem = new OrderItem.OrderItemBuilder()
                    .quantity(itemDTO.getQuantity())
                    .unitPrice(menuItem.getPrice())
                    .subtotal(menuItem.getPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity())))
                    .time(LocalDateTime.now())
                    .order(order)
                    .menuItem(menuItem)
                    .build();

            orderItems.add(orderItemRepository.save(orderItem));
        }

        order.setOrderItems(orderItems);
        return order;
    }




    //This is a test method, it wasn't used in the project
    @Override
    public BigDecimal calculatePendingOrdersAmount(Customer customer) {
        // Get all pending orders for the customer
        OrderStatus pendingStatus = orderStatusRepository.findByStatusType("PENDING")
                .orElseThrow(() -> new ResourceNotFoundException("Pending order status not found"));

        List<Order> pendingOrders = orderRepository.findByCustomerAndOrderStatus(customer, pendingStatus);

        // Calculate total amount of pending orders
        return pendingOrders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    @Transactional
    public Order cancelOrder(Long orderId, Long customerId) {
        // Validate order exists
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        // Validate order belongs to the customer
        if (!order.getCustomer().getId().equals(customerId)) {
            throw new IllegalArgumentException("Order does not belong to this customer");
        }

        // Check if order can be canceled (only pending orders can be canceled)
        if (!"PENDING".equals(order.getOrderStatus().getStatusType())) {
            throw new IllegalArgumentException("Only pending orders can be canceled");
        }

        // Get cancelled status
        OrderStatus cancelledStatus = orderStatusRepository.findByStatusType("CANCELLED")
                .orElseThrow(() -> new ResourceNotFoundException("Cancelled order status not found"));

        // Get the customer and restore their balance
        Customer customer = order.getCustomer();
        BigDecimal refundAmount = order.getTotalAmount();
        BigDecimal newBalance = customer.getBalance().add(refundAmount);
        customer.setBalance(newBalance);
        customerRepository.save(customer);

        // Update order status
        order.setOrderStatus(cancelledStatus);
        order.setCancelledAt(LocalDateTime.now());

        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public List<OrderDTO> getCustomerOrders(Long customerId) {
        // Validate customer exists
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));

        // Get customer orders
        List<Order> orders = orderRepository.findByCustomerOrderByOrderDateDesc(customer);

        // Convert to DTOs
        return orders.stream()
                .map(this::convertToOrderDTO)
                .collect(Collectors.toList());
    }


    @Override
    @Scheduled(cron = "0 0 2 * * *") // Runs at 2 AM every day
    @Transactional
    public void cleanupCancelledOrders() {
        // Find cancelled orders older than 7 days
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(7);
        OrderStatus cancelledStatus = orderStatusRepository.findByStatusType("CANCELLED")
                .orElseThrow(() -> new ResourceNotFoundException("Cancelled order status not found"));

        List<Order> oldCancelledOrders = orderRepository.findByCancelledAtBeforeAndOrderStatus(cutoffDate, cancelledStatus);
        // Delete the old cancelled orders
        if (!oldCancelledOrders.isEmpty()) {
            orderRepository.deleteAll(oldCancelledOrders);
            logger.info("Deleted {} cancelled orders older than 7 days", oldCancelledOrders.size());
        }
    }

    @Override
    public Long getTotalOrdersToday() {
        return orderRepository.getTotalOrdersToday();
    }

    @Override
    public Long getTotalServedOrdersToday() {
        return orderRepository.getTotalServedOrdersToday();
    }


    public OrderDTO convertToOrderDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setOrderId(order.getOrderId());
        dto.setOrderDate(order.getOrderDate());
        dto.setStatus(order.getOrderStatus().getStatusType());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setCancelledAt(order.getCancelledAt());
        dto.setServedAt(order.getServedAt());

        // Convert order items
        List<OrderItemDTO> itemDTOs = order.getOrderItems().stream()
                .map(item -> {
                    OrderItemDTO itemDTO = new OrderItemDTO();
                    itemDTO.setId(item.getId());
                    itemDTO.setMenuItemId(item.getMenuItem().getId());
                    itemDTO.setMenuItemName(item.getMenuItem().getName());
                    itemDTO.setQuantity(item.getQuantity());
                    itemDTO.setUnitPrice(item.getUnitPrice());
                    itemDTO.setSubtotal(item.getSubtotal());
                    itemDTO.setTime(item.getTime());
                    return itemDTO;
                })
                .collect(Collectors.toList());

        dto.setOrderItems(itemDTOs);

        return dto;
    }
}