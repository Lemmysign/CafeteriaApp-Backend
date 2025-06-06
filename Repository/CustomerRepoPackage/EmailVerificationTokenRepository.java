package Evercare_CafeteriaApp.Repository.CustomerRepoPackage;

import Evercare_CafeteriaApp.Model.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {

    Optional<EmailVerificationToken> findByToken(String token);

    boolean existsByToken(String token);
}
