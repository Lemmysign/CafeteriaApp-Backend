package Evercare_CafeteriaApp.Repository.AdminRepoPackage;
import Evercare_CafeteriaApp.Model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long>{

    @Query("SELECT a FROM Admin a WHERE LOWER(a.adminEmail) = LOWER(:adminEmail)")
    Optional<Admin> findByEmailIgnoreCase(@Param("adminEmail") String email);
    /* Find  Admin user by email (used for login & validation) */

    @Query("SELECT COUNT(c) FROM Customer c")
    long getTotalCustomerCount();

    @Query("SELECT COUNT(t) FROM Transaction t WHERE CAST(t.transactionDate AS date) = :date")
    long getTransactionCountForDate(@Param("date") LocalDate date);
}
