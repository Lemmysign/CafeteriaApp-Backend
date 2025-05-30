package Evercare_CafeteriaApp.DTO.ServerDtoPackage;

import java.math.BigDecimal;
import java.time.LocalDate;

// DTO for daily order statistics
public class DailyOrderStatisticsDTO {
    private LocalDate date;
    private Long totalOrders;
    private BigDecimal totalSales;

    // Default constructor
    public DailyOrderStatisticsDTO() {
    }

    // Constructor with fields
    public DailyOrderStatisticsDTO(LocalDate date, Long totalOrders, BigDecimal totalSales) {
        this.date = date;
        this.totalOrders = totalOrders;
        this.totalSales = totalSales;
    }

    // Getters and setters
    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Long getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(Long totalOrders) {
        this.totalOrders = totalOrders;
    }

    public BigDecimal getTotalSales() {
        return totalSales;
    }

    public void setTotalSales(BigDecimal totalSales) {
        this.totalSales = totalSales;
    }
}