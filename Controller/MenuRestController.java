package Evercare_CafeteriaApp.Controller;

import Evercare_CafeteriaApp.DTO.MenuDtoPackage.MenuCategoryDTO;
import Evercare_CafeteriaApp.DTO.MenuDtoPackage.MenuItemDTO;
import Evercare_CafeteriaApp.DTO.MenuDtoPackage.PageResponseDTO;
import Evercare_CafeteriaApp.Mapper.MenuMapper;
import Evercare_CafeteriaApp.Model.MenuCategory;
import Evercare_CafeteriaApp.Model.MenuItem;
import Evercare_CafeteriaApp.Services.MenuItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/menu")

public class MenuRestController {

    private final MenuItemService menuItemService;
    private final MenuMapper menuMapper;

    @Autowired
    public MenuRestController(MenuItemService menuItemService, MenuMapper menuMapper) {
        this.menuItemService = menuItemService;
        this.menuMapper = menuMapper;
    }

    @GetMapping("/categories")
    public ResponseEntity<List<MenuCategoryDTO>> getAllCategories() {
        List<MenuCategory> categories = menuItemService.getAllMenuCategories();
        List<MenuCategoryDTO> categoryDTOs = categories.stream()
                .map(category -> menuMapper.toMenuCategoryDTO(category, null))
                .collect(Collectors.toList());
        return ResponseEntity.ok(categoryDTOs);
    }

    @GetMapping("/items")
    public ResponseEntity<PageResponseDTO<MenuItemDTO>> getAllMenuItems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sort) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
        Page<MenuItem> menuItems = menuItemService.getAllAvailableMenuItems(pageable);
        PageResponseDTO<MenuItemDTO> response = menuMapper.toMenuItemPageDTO(menuItems);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/categories/{categoryName}/items")
    public ResponseEntity<PageResponseDTO<MenuItemDTO>> getMenuItemsByCategory(
            @PathVariable String categoryName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sort) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
        Page<MenuItem> menuItems = menuItemService.getMenuItemsByCategory(categoryName, pageable);
        PageResponseDTO<MenuItemDTO> response = menuMapper.toMenuItemPageDTO(menuItems);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/categories/id/{categoryId}/items")
    public ResponseEntity<PageResponseDTO<MenuItemDTO>> getMenuItemsByCategoryId(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sort) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
        Page<MenuItem> menuItems = menuItemService.getMenuItemsByCategoryId(categoryId, pageable);
        PageResponseDTO<MenuItemDTO> response = menuMapper.toMenuItemPageDTO(menuItems);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/categories/staff-alacarte")
    public ResponseEntity<List<MenuCategoryDTO>> getStaffAndAlaCarteMenu() {
        List<MenuCategory> categories = menuItemService.getAllMenuCategories().stream()
                .filter(category -> "STAFFMENU".equals(category.getName()) || "ALACARTE".equals(category.getName()))
                .toList();

        List<MenuCategoryDTO> categoryDTOs = categories.stream()
                .map(category -> {
                    // Get first page of menu items for each category
                    Page<MenuItem> menuItems = menuItemService.getMenuItemsByCategory(
                            category.getName(), PageRequest.of(0, 100));
                    return menuMapper.toMenuCategoryDTO(category, menuItems.getContent());
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(categoryDTOs);
    }

    @GetMapping("/item/{id}")
    public ResponseEntity<MenuItemDTO> getMenuItemById(@PathVariable Long id) {
        return menuItemService.getMenuItemById(id)
                .map(menuMapper::toMenuItemDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}