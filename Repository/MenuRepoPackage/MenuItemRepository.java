package Evercare_CafeteriaApp.Repository.MenuRepoPackage;

import Evercare_CafeteriaApp.Model.MenuItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import Evercare_CafeteriaApp.Model.MenuCategory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    // Find menu item by ID
    @SuppressWarnings("NullableProblems")
    Optional<MenuItem> findById(Long id);

    // Find menu items by menu category
    List<MenuItem> findByMenuCategory(MenuCategory menuCategory);

    // Find menu items by menu category with pagination
    Page<MenuItem> findByMenuCategory(MenuCategory menuCategory, Pageable pageable);

    // Find menu items by menu category ID
    List<MenuItem> findByMenuCategoryId(Long menuCategoryId);

    // Find menu items by menu category ID with pagination
    Page<MenuItem> findByMenuCategoryId(Long menuCategoryId, Pageable pageable);

    // Find menu items by availability
    List<MenuItem> findByIsAvailable(boolean isAvailable);

    // Find menu items by availability with pagination
    Page<MenuItem> findByIsAvailable(boolean isAvailable, Pageable pageable);

    // Find menu items by menu category and availability
    List<MenuItem> findByMenuCategoryAndIsAvailable(MenuCategory menuCategory, boolean isAvailable);

    // Find menu items by menu category and availability with pagination
    Page<MenuItem> findByMenuCategoryAndIsAvailable(MenuCategory menuCategory, boolean isAvailable, Pageable pageable);

    // Find menu items by menu category ID and availability
    List<MenuItem> findByMenuCategoryIdAndIsAvailable(Long menuCategoryId, boolean isAvailable);

    // Find menu items by menu category ID and availability with pagination
    Page<MenuItem> findByMenuCategoryIdAndIsAvailable(Long menuCategoryId, boolean isAvailable, Pageable pageable);

    // Find menu items by name containing the search term (case-insensitive)
    List<MenuItem> findByNameContainingIgnoreCase(String name);

    // Find menu items by name containing the search term with pagination
    Page<MenuItem> findByNameContainingIgnoreCase(String name, Pageable pageable);
}