package Evercare_CafeteriaApp.Model;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "menu_category")
public class MenuCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    // Default constructor
    public MenuCategory() {
    }

    // All-args constructor
    public MenuCategory(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    // Builder pattern
    public static class MenuCategoryBuilder {
        private Long id;
        private String name;

        public MenuCategoryBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public MenuCategoryBuilder name(String name) {
            this.name = name;
            return this;
        }

        public MenuCategory build() {
            return new MenuCategory(id, name);
        }
    }

    // Getters and Setters
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

    // equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MenuCategory that)) return false;
        return Objects.equals(id, that.id) &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    // toString
    @Override
    public String toString() {
        return "FoodCategory{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
