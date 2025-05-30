package Evercare_CafeteriaApp.DTO.AdminDtoPackage;

public class AdminDTO {
    private Long adminId;
    private String adminName;
    private String adminEmail;
    private String adminPhone;
    private String roleName; // Instead of full Role object, just store role name

    // Constructors
    public AdminDTO() {
    }

    public AdminDTO(Long adminId, String adminName, String adminEmail, String adminPhone, String roleName) {
        this.adminId = adminId;
        this.adminName = adminName;
        this.adminEmail = adminEmail;
        this.adminPhone = adminPhone;
        this.roleName = roleName;
    }

    // Getters and Setters
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

    public String getAdminPhone() {
        return adminPhone;
    }

    public void setAdminPhone(String adminPhone) {
        this.adminPhone = adminPhone;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
}
