package Evercare_CafeteriaApp.Repository.MenuRepoPackage;

import Evercare_CafeteriaApp.Model.StaffMenuList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StaffMenuListRepository extends JpaRepository<StaffMenuList, Long> {

    // Find all available foods for customer view
    List<StaffMenuList> findByIsAvailableTrue();

    // Find all foods by category ID - CORRECTED: Use the field name as it appears in entity
    @Query("SELECT s FROM StaffMenuList s WHERE s.menuCategory.id = :categoryId")
    List<StaffMenuList> findByCategoryId(@Param("categoryId") Long categoryId);

    // Find all foods by menu type ID - CORRECTED: Use the field name as it appears in entity
    @Query("SELECT s FROM StaffMenuList s WHERE s.menuType.id = :typeId")
    List<StaffMenuList> findByTypeId(@Param("typeId") Long typeId);

    // Custom query to optimize food loading with eager fetching for better performance
    @Query("SELECT s FROM StaffMenuList s LEFT JOIN FETCH s.menuCategory LEFT JOIN FETCH s.menuType ORDER BY s.foodName")
    List<StaffMenuList> findAllWithCategoryAndType();
}