package Evercare_CafeteriaApp.Model;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roleId;

    @Column(nullable = false, unique = true)
    private String roleName;


    public Role() {
    }


    public Role(Long roleId, String roleName) {
        this.roleId = roleId;
        this.roleName = roleName;
    }


    public static class RoleBuilder {
        private Long roleId;
        private String roleName;

        public RoleBuilder roleId(Long roleId) {
            this.roleId = roleId;
            return this;
        }

        public RoleBuilder roleName(String roleName) {
            this.roleName = roleName;
            return this;
        }

        public Role build() {
            return new Role(roleId, roleName);
        }
    }


    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Role role = (Role) o;
        return Objects.equals(roleId, role.roleId) && Objects.equals(roleName, role.roleName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roleId, roleName);
    }


    @Override
    public String toString() {
        return "Role{" +
                "roleId=" + roleId +
                ", roleName='" + roleName + '\'' +
                '}';
    }
}
