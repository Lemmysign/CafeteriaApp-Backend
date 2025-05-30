package Evercare_CafeteriaApp.ServiceImplementation;

import Evercare_CafeteriaApp.Model.MenuCategory;
import Evercare_CafeteriaApp.Model.MenuItem;
import Evercare_CafeteriaApp.Repository.MenuRepoPackage.MenuCategoryRepository;
import Evercare_CafeteriaApp.Repository.MenuRepoPackage.MenuItemRepository;
import Evercare_CafeteriaApp.Services.MenuItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MenuItemServiceImp implements MenuItemService {


    private final MenuItemRepository menuItemRepository;
    private final MenuCategoryRepository menuCategoryRepository;

    @Autowired
    public MenuItemServiceImp(MenuItemRepository menuItemRepository, MenuCategoryRepository menuCategoryRepository) {
        this.menuItemRepository = menuItemRepository;
        this.menuCategoryRepository = menuCategoryRepository;
    }

    public Page<MenuItem> getMenuItemsByCategory(String categoryName, Pageable pageable) {
        Optional<MenuCategory> category = menuCategoryRepository.findByName(categoryName);
        return category.map(menuCategory ->
                        menuItemRepository.findByMenuCategoryAndIsAvailable(menuCategory, true, pageable))
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + categoryName));
    }

    public Page<MenuItem> getMenuItemsByCategoryId(Long categoryId, Pageable pageable) {
        return menuItemRepository.findByMenuCategoryIdAndIsAvailable(categoryId, true, pageable);
    }

    public Page<MenuItem> getAllAvailableMenuItems(Pageable pageable) {
        return menuItemRepository.findByIsAvailable(true, pageable);
    }

    public List<MenuCategory> getAllMenuCategories() {
        return menuCategoryRepository.findAll();
    }

    public Optional<MenuItem> getMenuItemById(Long id) {
        return menuItemRepository.findById(id);
    }
}
