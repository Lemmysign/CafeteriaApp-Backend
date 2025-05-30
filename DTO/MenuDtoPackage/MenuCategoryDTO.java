package Evercare_CafeteriaApp.DTO.MenuDtoPackage;

import java.util.List;

public class MenuCategoryDTO {
    private Long id;
    private String name;
    private List<MenuItemDTO> menuItems;

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<MenuItemDTO> getMenuItems() {
        return menuItems;
    }

    public void setMenuItems(List<MenuItemDTO> menuItems) {
        this.menuItems = menuItems;
    }
}