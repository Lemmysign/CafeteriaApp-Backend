package Evercare_CafeteriaApp.Repository.MenuRepoPackage;

import Evercare_CafeteriaApp.Model.Customer;
import Evercare_CafeteriaApp.Model.MenuCategory;
import Evercare_CafeteriaApp.Model.MenuType;
import Evercare_CafeteriaApp.Model.StaffMenuList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface MenuTypeRepository extends JpaRepository<MenuType, Long> {

    @Query("SELECT c FROM MenuType c WHERE LOWER(c.menuType) = LOWER(:menuType)")
    Optional<MenuType> findByNameIgnoreCase(@Param("menuType") String menuType);


}
