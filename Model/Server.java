package Evercare_CafeteriaApp.Model;

import jakarta.persistence.*;
import org.hibernate.annotations.NaturalId;
import java.util.Objects;

@Entity
@Table(name = "servers")
public class Server {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "server_id")
    private Long serverId;

    @Column(nullable = false)
    private String serverName;

    @NaturalId
    @Column(nullable = false, unique = true)
    private String serverEmail;

    @Column(nullable = false)
    private String serverPassword;

    @Column(nullable = false)
    private String serverPhone;

    private boolean isServerBlocked;

    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;


    public Server() {
    }


    public Server(Long serverId, String serverName, String serverEmail, String serverPassword, String serverPhone, boolean isServerBlocked, Role role) {
        this.serverId = serverId;
        this.serverName = serverName;
        this.serverEmail = serverEmail;
        this.serverPassword = serverPassword;
        this.serverPhone = serverPhone;
        this.isServerBlocked = isServerBlocked;
        this.role = role;
    }

    // 🔹 Builder Pattern
    public static class ServerBuilder {
        private Long serverId;
        private String serverName;
        private String serverEmail;
        private String serverPassword;
        private String serverPhone;
        private boolean isServerBlocked;
        private Role role;

        public ServerBuilder serverId(Long serverId) {
            this.serverId = serverId;
            return this;
        }

        public ServerBuilder serverName(String serverName) {
            this.serverName = serverName;
            return this;
        }

        public ServerBuilder serverEmail(String serverEmail) {
            this.serverEmail = serverEmail;
            return this;
        }

        public ServerBuilder serverPassword(String serverPassword) {
            this.serverPassword = serverPassword;
            return this;
        }

        public ServerBuilder serverPhone(String serverPhone) {
            this.serverPhone = serverPhone;
            return this;
        }

        public ServerBuilder isServerBlocked(boolean isServerBlocked) {
            this.isServerBlocked = isServerBlocked;
            return this;
        }

        public ServerBuilder role(Role role) {
            this.role = role;
            return this;
        }

        public Server build() {
            return new Server(serverId, serverName, serverEmail, serverPassword, serverPhone, isServerBlocked, role);
        }
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

    public String getServerPassword() {
        return serverPassword;
    }

    public void setServerPassword(String serverPassword) {
        this.serverPassword = serverPassword;
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
        Server server = (Server) o;
        return isServerBlocked == server.isServerBlocked &&
                Objects.equals(serverId, server.serverId) &&
                Objects.equals(serverName, server.serverName) &&
                Objects.equals(serverEmail, server.serverEmail) &&
                Objects.equals(serverPassword, server.serverPassword) &&
                Objects.equals(serverPhone, server.serverPhone) &&
                Objects.equals(role, server.role);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serverId, serverName, serverEmail, serverPassword, serverPhone, isServerBlocked, role);
    }


    @Override
    public String toString() {
        return "Server{" +
                "serverId=" + serverId +
                ", serverName='" + serverName + '\'' +
                ", serverEmail='" + serverEmail + '\'' +
                ", serverPassword='" + serverPassword + '\'' +
                ", serverPhone='" + serverPhone + '\'' +
                ", isServerBlocked=" + isServerBlocked +
                ", role=" + role +
                '}';
    }
}
