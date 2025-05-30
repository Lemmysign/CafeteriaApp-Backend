package Evercare_CafeteriaApp.Repository.PaymentRepoPackage;

import Evercare_CafeteriaApp.Model.TransferHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@SuppressWarnings("NullableProblems")
@Repository
public interface TransferHistoryRepository extends JpaRepository<TransferHistory, Long> {
    Optional<TransferHistory> findById(Long id);
}
