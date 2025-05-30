package Evercare_CafeteriaApp.Model;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "menu_type")
public class MenuType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "menu_type", nullable = false, unique = true)
    private String menuType;

    // Default constructor
    public MenuType() {
    }

    // All-args constructor
    public MenuType(Long id, String menuType) {
        this.id = id;
        this.menuType = menuType;
    }

    // Builder
    public static class Builder {
        private Long id;
        private String menuType;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder menuType(String menuType) {
            this.menuType = menuType;
            return this;
        }

        public MenuType build() {
            return new MenuType(id, menuType);
        }
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMenuType() {
        return menuType;
    }

    public void setMenuType(String menuType) {
        this.menuType = menuType;
    }

    // equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MenuType)) return false;
        MenuType that = (MenuType) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(menuType, that.menuType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, menuType);
    }

    // toString
    @Override
    public String toString() {
        return "MenuType{" +
                "id=" + id +
                ", menuType='" + menuType + '\'' +
                '}';
    }
}
