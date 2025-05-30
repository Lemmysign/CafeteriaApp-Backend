package Evercare_CafeteriaApp.Repository.PaymentRepoPackage;

import Evercare_CafeteriaApp.Model.Customer;
import Evercare_CafeteriaApp.Model.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
    Optional<PaymentTransaction> findByReference(String reference);
    List<PaymentTransaction> findByCustomer(Customer customer);
    List<PaymentTransaction> findByCustomerOrderByCreatedAtDesc(Customer customer);


}