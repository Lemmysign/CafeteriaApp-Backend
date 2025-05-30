package Evercare_CafeteriaApp.Model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "menu_items")
public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    private boolean isAvailable;

    @ManyToOne
    @JoinColumn(name = "menuCategory_id", nullable = false)
    private MenuCategory menuCategory;

    @OneToMany(mappedBy = "menuItem")
    private Set<StaffMenuList> staffMenuListset = new HashSet<>();

    @OneToMany(mappedBy = "menuItem")
    private Set<OrderItem> orderItems = new HashSet<>();

    public MenuItem() {}

    public MenuItem(Long id, String name, BigDecimal price, boolean isAvailable, Set<OrderItem> orderItems) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.isAvailable = isAvailable;
        this.orderItems = orderItems;
    }

    public Set<StaffMenuList> getStaffMenuListset() {
        return staffMenuListset;
    }

    public void setStaffMenuListset(Set<StaffMenuList> staffMenuListset) {
        this.staffMenuListset = staffMenuListset;
    }

    public static class MenuItemBuilder {
        private Long id;
        private String name;
        private BigDecimal price;
        private boolean isAvailable;
        private Set<OrderItem> orderItems = new HashSet<>();

        public MenuItemBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public MenuItemBuilder name(String name) {
            this.name = name;
            return this;
        }

        public MenuItemBuilder description(String description) {
            return this;
        }

        public MenuItemBuilder price(BigDecimal price) {
            this.price = price;
            return this;
        }

        public MenuItemBuilder isAvailable(boolean isAvailable) {
            this.isAvailable = isAvailable;
            return this;
        }

        public MenuItemBuilder orderItems(Set<OrderItem> orderItems) {
            this.orderItems = orderItems;
            return this;
        }

        public MenuItem build() {
            return new MenuItem(id, name, price, isAvailable, orderItems);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }



    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }

    public Set<OrderItem> getOrderItems() { return orderItems; }
    public void setOrderItems(Set<OrderItem> orderItems) { this.orderItems = orderItems; }

    public MenuCategory getMenuCategory() {
        return menuCategory;
    }

    public void setMenuCategory(MenuCategory menuCategory) {
        this.menuCategory = menuCategory;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MenuItem menuItem = (MenuItem) o;
        return Objects.equals(price, menuItem.price) &&
                isAvailable == menuItem.isAvailable &&
                Objects.equals(id, menuItem.id) &&
                Objects.equals(name, menuItem.name) &&
                Objects.equals(orderItems, menuItem.orderItems);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, price, isAvailable, orderItems);
    }

    @Override
    public String toString() {
        return "MenuItem{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", isAvailable=" + isAvailable +
                ", orderItems=" + orderItems +
                '}';
    }
}
