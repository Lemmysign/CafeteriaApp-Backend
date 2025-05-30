package Evercare_CafeteriaApp.Repository.OrderRepoPackage;

import Evercare_CafeteriaApp.Model.Customer;
import Evercare_CafeteriaApp.Model.Order;
import Evercare_CafeteriaApp.Model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerId(Long customerId);
    Optional<Order> findByOrderIdAndCustomerId(Long orderId, Long customerId);
    Page<Order> findByCustomerIdOrderByOrderDateDesc(Long customerId, Pageable pageable);
    List<Order> findByCustomerOrderByOrderDateDesc(Customer customer);
    List<Order> findByCustomerAndOrderStatus(Customer customer, OrderStatus orderStatus);
    List<Order> findByCancelledAtBeforeAndOrderStatus(LocalDateTime cutoffDate, OrderStatus status);
    @Query("SELECT o FROM Order o JOIN FETCH o.orderItems WHERE o.customer = :customer AND o.orderStatus = :status")
    List<Order> findByCustomerAndOrderStatusWithItems(@Param("customer") Customer customer, @Param("status") OrderStatus status);

    @Query(value = "SELECT COUNT(*) FROM orders WHERE DATE(order_date) = CURRENT_DATE", nativeQuery = true)
    Long getTotalOrdersToday();

    @Query(value = "SELECT COUNT(*) FROM orders WHERE DATE(served_at) = CURRENT_DATE", nativeQuery = true)
    Long getTotalServedOrdersToday();

}