package Evercare_CafeteriaApp.Model;

import jakarta.persistence.*;
import org.hibernate.annotations.NaturalId;
import java.util.Objects;

@Entity
@Table(name = "admins")
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long adminId;

    @Column(nullable = false)
    private String adminName;

    @NaturalId
    @Column(nullable = false, unique = true)
    private String adminEmail;

    @Column(nullable = false)
    private String adminPassword;

    @Column(nullable = false)
    private String adminPhone;

    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;


    public Admin() {}

    public Admin(Long adminId, String adminName, String adminEmail, String adminPassword, String adminPhone, Role role) {
        this.adminId = adminId;
        this.adminName = adminName;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
        this.adminPhone = adminPhone;
        this.role = role;
    }

    @SuppressWarnings("unused")
    public static class AdminBuilder {
        private Long adminId;
        private String adminName;
        private String adminEmail;
        private String adminPassword;
        private String adminPhone;
        private Role role;

        public AdminBuilder adminId(Long adminId) {
            this.adminId = adminId;
            return this;
        }

        public AdminBuilder adminName(String adminName) {
            this.adminName = adminName;
            return this;
        }

        public AdminBuilder adminEmail(String adminEmail) {
            this.adminEmail = adminEmail;
            return this;
        }

        public AdminBuilder adminPassword(String adminPassword) {
            this.adminPassword = adminPassword;
            return this;
        }

        public AdminBuilder adminPhone(String adminPhone) {
            this.adminPhone = adminPhone;
            return this;
        }

        public AdminBuilder role(Role role) {
            this.role = role;
            return this;
        }

        public Admin build() {
            return new Admin(adminId, adminName, adminEmail, adminPassword, adminPhone, role);
        }
    }

    public Long getAdminId() {
        return adminId;
    }

    public void setAdminId(Long adminId) {
        this.adminId = adminId;
    }

    public String getAdminName() {
        return adminName;
    }

    public void setAdminName(String adminName) {
        this.adminName = adminName;
    }

    public String getAdminEmail() {
        return adminEmail;
    }

    public void setAdminEmail(String adminEmail) {
        this.adminEmail = adminEmail;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }

    public String getAdminPhone() {
        return adminPhone;
    }

    public void setAdminPhone(String adminPhone) {
        this.adminPhone = adminPhone;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }


    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Admin admin = (Admin) o;
        return Objects.equals(adminId, admin.adminId) &&
                Objects.equals(adminName, admin.adminName) &&
                Objects.equals(adminEmail, admin.adminEmail) &&
                Objects.equals(adminPassword, admin.adminPassword) &&
                Objects.equals(adminPhone, admin.adminPhone) &&
                Objects.equals(role, admin.role);
    }

    @Override
    public int hashCode() {
        return Objects.hash(adminId, adminName, adminEmail, adminPassword, adminPhone, role);
    }


    @Override
    public String toString() {
        return "Admin{" +
                "adminId=" + adminId +
                ", adminName='" + adminName + '\'' +
                ", adminEmail='" + adminEmail + '\'' +
                ", adminPassword='" + adminPassword + '\'' +
                ", adminPhone='" + adminPhone + '\'' +
                ", role=" + role +
                '}';
    }
}
