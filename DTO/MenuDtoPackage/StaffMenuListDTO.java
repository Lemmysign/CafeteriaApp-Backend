package Evercare_CafeteriaApp.DTO.MenuDtoPackage;

import Evercare_CafeteriaApp.Model.StaffMenuList;

public class StaffMenuListDTO {
    private Long foodId;
    private String foodName;
    private boolean isAvailable;
    private Long menuItemId;
    private String menuItemName;
    private Long categoryId;
    private String categoryName;
    private Long typeId;
    private String typeName;

    // Default constructor
    public StaffMenuListDTO() {
    }

    // Constructor from entity - corrected to match actual model properties
    public StaffMenuListDTO(StaffMenuList staffMenuList) {
        this.foodId = staffMenuList.getFoodId();
        this.foodName = staffMenuList.getFoodName();
        this.isAvailable = staffMenuList.isAvailable();

        if (staffMenuList.getMenuItem() != null) {
            this.menuItemId = staffMenuList.getMenuItem().getId();
            this.menuItemName = staffMenuList.getMenuItem().getName();
        }

        if (staffMenuList.getMenuCategory() != null) {
            // Assuming the actual property names in MenuCategory class
            this.categoryId = staffMenuList.getMenuCategory().getId();
            this.categoryName = staffMenuList.getMenuCategory().getName();
        }

        if (staffMenuList.getMenuType() != null) {
            // Assuming the actual property names in MenuType class
            this.typeId = staffMenuList.getMenuType().getId();
            this.typeName = staffMenuList.getMenuType().getMenuType();
        }
    }

    // Getters and setters
    public Long getFoodId() {
        return foodId;
    }

    public void setFoodId(Long foodId) {
        this.foodId = foodId;
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public Long getMenuItemId() {
        return menuItemId;
    }

    public void setMenuItemId(Long menuItemId) {
        this.menuItemId = menuItemId;
    }

    public String getMenuItemName() {
        return menuItemName;
    }

    public void setMenuItemName(String menuItemName) {
        this.menuItemName = menuItemName;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Long getTypeId() {
        return typeId;
    }

    public void setTypeId(Long typeId) {
        this.typeId = typeId;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }
}