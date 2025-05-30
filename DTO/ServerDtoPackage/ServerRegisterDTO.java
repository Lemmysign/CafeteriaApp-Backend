package Evercare_CafeteriaApp.DTO.ServerDtoPackage;

public class ServerRegisterDTO {
    private Long serverId;
    private String serverName;
    private String serverEmail;
    private String serverPhone;
    private String serverPassword;

    public ServerRegisterDTO() {
    }

    public ServerRegisterDTO(Long serverId, String serverName, String serverEmail, String serverPhone, String serverPassword) {
        this.serverId = serverId;
        this.serverName = serverName;
        this.serverEmail = serverEmail;
        this.serverPhone = serverPhone;
        this.serverPassword = serverPassword;
    }

    // Getters and Setters
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

    public String getServerPassword() {
        return serverPassword;
    }

    public void setServerPassword(String serverPassword) {
        this.serverPassword = serverPassword;
    }
}