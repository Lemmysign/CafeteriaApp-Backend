package Evercare_CafeteriaApp.DTO.ServerDtoPackage;

import java.math.BigDecimal;

public class ServerSummaryDTO {

    private Long id;
    private String name;
    private boolean blocked;

    public ServerSummaryDTO() {
    }

    public ServerSummaryDTO(Long id, String name, boolean blocked) {
        this.id = id;
        this.name = name;
        this.blocked = blocked;
    }

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

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }
}
