package Evercare_CafeteriaApp.Services;

import Evercare_CafeteriaApp.Model.MenuCategory;
import Evercare_CafeteriaApp.Model.MenuItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface MenuItemService {

    Page<MenuItem> getMenuItemsByCategory(String categoryName, Pageable pageable);

    Page<MenuItem> getMenuItemsByCategoryId(Long categoryId, Pageable pageable);

    Page<MenuItem> getAllAvailableMenuItems(Pageable pageable);

    List<MenuCategory> getAllMenuCategories();

    Optional<MenuItem> getMenuItemById(Long id);
}
