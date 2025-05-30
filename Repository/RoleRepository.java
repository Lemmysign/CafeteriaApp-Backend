package Evercare_CafeteriaApp.Repository;

import Evercare_CafeteriaApp.Model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    // Find role by role name
    Optional<Role> findByRoleName(String roleName);
}
