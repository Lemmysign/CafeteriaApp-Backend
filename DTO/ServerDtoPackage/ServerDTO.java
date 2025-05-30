package Evercare_CafeteriaApp.DTO.ServerDtoPackage;

public class ServerDTO {
    private Long serverId;
    private String serverName;
    private String serverEmail;
    private String serverPhone;
    private boolean isServerBlocked;
    private String roleName; // Extracted from Role entity


    public ServerDTO() {
    }


    public ServerDTO(Long serverId, String serverName, String serverEmail, String serverPhone, boolean isServerBlocked, String roleName) {
        this.serverId = serverId;
        this.serverName = serverName;
        this.serverEmail = serverEmail;
        this.serverPhone = serverPhone;
        this.isServerBlocked = isServerBlocked;
        this.roleName = roleName;
    }


    public Long getServerId() {
        return serverId;
    }

    public void setServerId(Long serverId) {
        this.serverId = serverId;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getServerEmail() {
        return serverEmail;
    }

    public void setServerEmail(String serverEmail) {
        this.serverEmail = serverEmail;
    }

    public String getServerPhone() {
        return serverPhone;
    }

    public void setServerPhone(String serverPhone) {
        this.serverPhone = serverPhone;
    }

    public boolean isServerBlocked() {
        return isServerBlocked;
    }

    public void setServerBlocked(boolean serverBlocked) {
        isServerBlocked = serverBlocked;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
}
