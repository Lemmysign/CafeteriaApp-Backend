package Evercare_CafeteriaApp.Services;

import Evercare_CafeteriaApp.DTO.AdminDtoPackage.DisplayAdminNameIdDTO;
import Evercare_CafeteriaApp.DTO.CustomerDtoPackage.CustomerRegisterDTO;
import Evercare_CafeteriaApp.DTO.CustomerDtoPackage.CustomerSummaryDTO;
import Evercare_CafeteriaApp.DTO.ServerDtoPackage.ServerRegisterDTO;
import Evercare_CafeteriaApp.DTO.ServerDtoPackage.ServerSummaryDTO;
import Evercare_CafeteriaApp.Model.Admin;
import Evercare_CafeteriaApp.Model.Customer;
import Evercare_CafeteriaApp.Model.Server;
import Evercare_CafeteriaApp.Model.Transaction;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface AdminService {

    Admin login(String email, String password);
    void logout(HttpServletRequest request, HttpServletResponse response);

    Server registerServer(ServerRegisterDTO serverRegisterDTO);

    DisplayAdminNameIdDTO getAdminBasicInfoByEmail(String email);

    Admin findByEmail(String email);

    long getTotalCustomerCount();

    long getTransactionCountForDate(LocalDate date);
    long getTransactionCountForToday();

    Boolean toggleStaffAccountBlock(Long id);

    Boolean toggleServersAccountBlock(Long id);

    Page<CustomerSummaryDTO> findCustomersByIdOrName(String searchTerm, Pageable pageable);

    List<CustomerSummaryDTO> getAllBlockedCustomers();

    Page<ServerSummaryDTO> findServersByIdOrName(String searchTerm, Pageable pageable);

    List<ServerSummaryDTO> getAllBlockedServers();

    // Original methods - keep for backward compatibility
    List<Transaction> getTransactionsForPeriod(String periodType, LocalDate referenceDate, Integer year, Integer month, LocalDateTime endDate);
    List<Transaction> getTransactionsForDay(LocalDate date);
    List<Transaction> getTransactionsForToday();
    List<Transaction> getTransactionsForWeek(LocalDate date);
    List<Transaction> getTransactionsForCurrentWeek();
    List<Transaction> getTransactionsForMonth(int year, int month);
    List<Transaction> getTransactionsForCurrentMonth();
    List<Transaction> getTransactionsForYear(int year);
    List<Transaction> getTransactionsForCurrentYear();
    List<Transaction> getTransactionsForDateRange(LocalDateTime startDate, LocalDateTime endDate);

    // New pagination-enabled methods
    Page<Transaction> getTransactionsForPeriodPaginated(String periodType, LocalDate referenceDate, Integer year, Integer month, LocalDateTime endDate, Pageable pageable);
    Page<Transaction> getTransactionsForDayPaginated(LocalDate date, Pageable pageable);
    Page<Transaction> getTransactionsForTodayPaginated(Pageable pageable);
    Page<Transaction> getTransactionsForWeekPaginated(LocalDate date, Pageable pageable);
    Page<Transaction> getTransactionsForCurrentWeekPaginated(Pageable pageable);
    Page<Transaction> getTransactionsForMonthPaginated(int year, int month, Pageable pageable);
    Page<Transaction> getTransactionsForCurrentMonthPaginated(Pageable pageable);
    Page<Transaction> getTransactionsForYearPaginated(int year, Pageable pageable);
    Page<Transaction> getTransactionsForCurrentYearPaginated(Pageable pageable);
    Page<Transaction> getTransactionsForDateRangePaginated(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);




}
