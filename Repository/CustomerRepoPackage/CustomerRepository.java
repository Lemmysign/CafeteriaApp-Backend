package Evercare_CafeteriaApp.Repository.CustomerRepoPackage;

import Evercare_CafeteriaApp.DTO.CustomerDtoPackage.CustomerSummaryDTO;
import Evercare_CafeteriaApp.Model.Customer;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    @Query("SELECT c FROM Customer c WHERE LOWER(c.email) = LOWER(:email)")
    Optional<Customer> findByEmailIgnoreCase(@Param("email") String email);
    //Find a customer by email (used for login & validation)


    //Check if a customer exists by email (used for registration)
    boolean existsByEmail(@Param("email") String email);


    //Get all customers who are NOT locked
    @Query("SELECT c FROM Customer c WHERE c.isLocked = false")
    List<Customer> findAllUnlockedCustomers();

    //Get all customers who are not locked
    @Query("SELECT c FROM Customer c WHERE c.isLocked = false ")
    List<Customer> findAllByLockedFalse();

    @Modifying
    @Transactional
    @Query("DELETE FROM Customer c WHERE c.email = :email")
    void deleteByEmail(@Param("email") String email);

    //find all customers by name or id using pagination

    @Query("SELECT new Evercare_CafeteriaApp.DTO.CustomerDtoPackage.CustomerSummaryDTO(c.id, c.name, c.balance, c.isBlocked) " +
            "FROM Customer c WHERE " +
            "(:searchTerm IS NULL OR :searchTerm = '') OR " +
            "CAST(c.id AS string) = :searchTerm OR " +
            "LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<CustomerSummaryDTO> findCustomersByIdOrName(@Param("searchTerm") String searchTerm, Pageable pageable);

   //find all blocked customers using list
   @Query("SELECT new Evercare_CafeteriaApp.DTO.CustomerDtoPackage.CustomerSummaryDTO(c.id, c.name, c.balance, c.isBlocked) " +
           "FROM Customer c WHERE c.isBlocked = true")
   List<CustomerSummaryDTO> findAllBlockedCustomers();


    //find all blocked users using pagination
    @Query("SELECT new map(c.id as id, c.name as name, c.balance as balance, c.isBlocked as blocked) " +
            "FROM Customer c WHERE c.isBlocked = true")
    Page<Object> findAllBlockedCustomersPaginated(Pageable pageable);
}