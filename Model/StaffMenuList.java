package Evercare_CafeteriaApp.Model;

import jakarta.persistence.*;

@Entity
@Table(name = "staff_menu_list")
public class StaffMenuList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long foodId;

    @Column(nullable = false)
    private String foodName;

    @Column(nullable = false)
    private boolean isAvailable;

    @ManyToOne
    @JoinColumn(name = "menu_item_id")
    private MenuItem menuItem;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private MenuCategory menuCategory;

    @ManyToOne
    @JoinColumn(name = "menu_type_id", nullable = false)
    private MenuType menuType;

    // Default constructor
    public StaffMenuList() {
    }

    // All-args constructor
    public StaffMenuList(Long foodId, String foodName, boolean isAvailable, MenuItem menuItem,
                         MenuCategory menuCategory, MenuType menuType) {
        this.foodId = foodId;
        this.foodName = foodName;
        this.isAvailable = isAvailable;
        this.menuItem = menuItem;
        this.menuCategory = menuCategory;
        this.menuType = menuType;
    }

    // Getters and Setters
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

    public MenuItem getMenuItem() {
        return menuItem;
    }

    public void setMenuItem(MenuItem menuItem) {
        this.menuItem = menuItem;
    }

    public MenuCategory getMenuCategory() {
        return menuCategory;
    }

    public void setMenuCategory(MenuCategory menuCategory) {
        this.menuCategory = menuCategory;
    }

    public MenuType getMenuType() {
        return menuType;
    }

    public void setMenuType(MenuType menuType) {
        this.menuType = menuType;
    }

    @Override
    public String toString() {
        return "StaffMenuList{" +
                "foodId=" + foodId +
                ", foodName='" + foodName + '\'' +
                ", isAvailable=" + isAvailable +
                ", menuItem=" + menuItem +
                ", menuCategory=" + menuCategory +
                ", menuType=" + menuType +
                '}';
    }
}