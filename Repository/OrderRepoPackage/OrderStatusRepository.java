package Evercare_CafeteriaApp.Repository.OrderRepoPackage;

import Evercare_CafeteriaApp.Model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderStatusRepository extends JpaRepository<OrderStatus, Long> {
    Optional<OrderStatus> findByStatusType(String statusType);
}