package Evercare_CafeteriaApp.ServiceImplementation;
import Evercare_CafeteriaApp.DTO.AdminDtoPackage.DisplayAdminNameIdDTO;
import Evercare_CafeteriaApp.DTO.CustomerDtoPackage.CustomerRegisterDTO;
import Evercare_CafeteriaApp.DTO.CustomerDtoPackage.CustomerSummaryDTO;
import Evercare_CafeteriaApp.DTO.ServerDtoPackage.ServerRegisterDTO;
import Evercare_CafeteriaApp.DTO.ServerDtoPackage.ServerSummaryDTO;
import Evercare_CafeteriaApp.Mapper.AdminMapper;
import Evercare_CafeteriaApp.Mapper.CustomerMapper;
import Evercare_CafeteriaApp.Mapper.ServerMapper;
import Evercare_CafeteriaApp.Model.*;
import Evercare_CafeteriaApp.Repository.AdminRepoPackage.AdminRepository;
import Evercare_CafeteriaApp.Repository.CustomerRepoPackage.CustomerRepository;
import Evercare_CafeteriaApp.Repository.CustomerRepoPackage.EmailVerificationTokenRepository;
import Evercare_CafeteriaApp.Repository.PaymentRepoPackage.TransactionRepository;
import Evercare_CafeteriaApp.Repository.RoleRepository;
import Evercare_CafeteriaApp.Repository.ServiceRepoPackage.ServerRepository;
import Evercare_CafeteriaApp.Services.AdminService;
import Evercare_CafeteriaApp.Services.EmailVerificationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.UUID;


@Service
public class AdminServiceImpl implements AdminService {

    private final ServerRepository serverRepository;

    private final CustomerRepository customerRepository;

    private final CustomerMapper customerMapper;

    private final EmailVerificationService emailVerificationService;

    private final ServerMapper serverMapper;

    private final PasswordEncoder passwordEncoder;

    private final RoleRepository roleRepository;

    private final AdminRepository adminRepository;

    private final TransactionRepository transactionRepository;

    private final AdminMapper adminMapper;

    private final EmailVerificationTokenRepository emailVerificationTokenRepository;

    @Autowired
    public AdminServiceImpl(ServerRepository serverRepository, CustomerRepository customerRepository, CustomerMapper customerMapper, EmailVerificationService emailVerificationService, ServerMapper serverMapper, PasswordEncoder passwordEncoder, RoleRepository roleRepository, AdminRepository adminRepository, TransactionRepository transactionRepository, AdminMapper adminMapper, EmailVerificationTokenRepository emailVerificationTokenRepository) {
        this.serverRepository = serverRepository;
        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
        this.emailVerificationService = emailVerificationService;
        this.serverMapper = serverMapper;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.adminRepository = adminRepository;
        this.transactionRepository = transactionRepository;
        this.adminMapper = adminMapper;
        this.emailVerificationTokenRepository = emailVerificationTokenRepository;
    }


    @Override
    public Admin findByEmail(String email) {
        return adminRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("Admin not found"));
    }


    @Override
    public Admin login(String email, String password) {
        Admin admin = adminRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("Invalid email or password!"));

        Role adminRole = roleRepository.findByRoleName("ADMIN")
                .orElseThrow(() -> new RuntimeException("Default role ADMIN not found!"));


        if (!passwordEncoder.matches(password, admin.getAdminPassword())) {

            if (admin.getAdminPassword().equals(password)) {
                // Hash and update the stored password
                admin.setAdminPassword(passwordEncoder.encode(password));
                adminRepository.save(admin);
            } else {
                throw new RuntimeException("Invalid email or password!");
            }
        }
        adminMapper.fromAdmin(admin);
        return admin;
    }




    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false); // Get current session if exists
        if (session != null) {
            session.invalidate(); // Invalidate the session
        }
    }




    @Override
    public Server registerServer(ServerRegisterDTO serverRegisterDTO) {
        if (serverRepository.existsByServerEmail(serverRegisterDTO.getServerEmail())) {
            throw new RuntimeException("Email already exists! Please use a different email.");
        }

        Role serverRole = roleRepository.findByRoleName("SERVER")
                .orElseThrow(() -> new RuntimeException("Default role SERVER not found!"));

        Server server = serverMapper.toEntity(serverRegisterDTO);
        server.setServerPassword(passwordEncoder.encode(serverRegisterDTO.getServerPassword())); // Encode password before saving

        server.setRole(serverRole);

        server = serverRepository.save(server);

        return server;

    }

    @Override
    public DisplayAdminNameIdDTO getAdminBasicInfoByEmail(String email) {
        Admin admin = adminRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("Admin not found"));
        return new DisplayAdminNameIdDTO(admin.getAdminName(),admin.getAdminId());
    }

    @Override
    public long getTotalCustomerCount() {
        return adminRepository.getTotalCustomerCount();
    }

    @Override
    public long getTransactionCountForDate(LocalDate date) {
        return adminRepository.getTransactionCountForDate(date);
    }

    @Override
    public long getTransactionCountForToday() {
        return getTransactionCountForDate(LocalDate.now());
    }

    @Override
    public Boolean toggleStaffAccountBlock(Long id) {
        Customer customer = customerRepository.findById(id).orElseThrow(() -> new RuntimeException("Invalid id!"));


        boolean currentlyBlocked = customer.isBlocked();
        boolean newBlockStatus = !currentlyBlocked;

        customer.setBlocked(newBlockStatus);

        if (!newBlockStatus) {
            // If the account is being unlocked now, record the time
            customer.setLastActionTime(LocalDateTime.now());
        }

        customerRepository.save(customer);

        return customer.isBlocked();
    }

    @Override
    public Boolean toggleServersAccountBlock(Long id) {
        Server server = serverRepository.findById(id).orElseThrow(() -> new RuntimeException("Invalid id!"));

        boolean currentlyBlocked = server.isServerBlocked();
        boolean newServerBlockStatus = !currentlyBlocked;

        server.setServerBlocked(newServerBlockStatus);


        serverRepository.save(server);

        return server.isServerBlocked();
    }

    @Override
    public Page<CustomerSummaryDTO> findCustomersByIdOrName(String searchTerm, Pageable pageable) {
        return customerRepository.findCustomersByIdOrName(searchTerm, pageable);
    }


    @Override
    public List<CustomerSummaryDTO> getAllBlockedCustomers() {
        return customerRepository.findAllBlockedCustomers();
    }

    @Override
    public Page<ServerSummaryDTO> findServersByIdOrName(String searchTerm, Pageable pageable) {
        return serverRepository.findServersByIdOrName(searchTerm, pageable);
    }

    @Override
    public List<ServerSummaryDTO> getAllBlockedServers() {
        return serverRepository.findAllBlockedServers();
    }


    public ServerMapper getServerMapper() {
        return serverMapper;
    }

    public ServerRepository getServerRepository() {
        return serverRepository;
    }

    public CustomerRepository getCustomerRepository() {
        return customerRepository;
    }

    public CustomerMapper getCustomerMapper() {
        return customerMapper;
    }

    public EmailVerificationService getEmailVerificationService() {
        return emailVerificationService;
    }

    public EmailVerificationTokenRepository getEmailVerificationTokenRepository() {
        return emailVerificationTokenRepository;
    }


    @Override
    public List<Transaction> getTransactionsForPeriod(String periodType, LocalDate referenceDate,
                                                      Integer year, Integer month, LocalDateTime endDate) {
        DateRangeResult dateRange = calculateDateRange(periodType, referenceDate, year, month, endDate);
        return transactionRepository.findByTransactionDateBetween(dateRange.startDateTime, dateRange.endDateTime);
    }

    @Override
    public List<Transaction> getTransactionsForDay(LocalDate date) {
        return getTransactionsForPeriod("day", date, null, null, null);
    }

    @Override
    public List<Transaction> getTransactionsForToday() {
        return getTransactionsForDay(LocalDate.now());
    }

    @Override
    public List<Transaction> getTransactionsForWeek(LocalDate date) {
        return getTransactionsForPeriod("week", date, null, null, null);
    }

    @Override
    public List<Transaction> getTransactionsForCurrentWeek() {
        return getTransactionsForWeek(LocalDate.now());
    }

    @Override
    public List<Transaction> getTransactionsForMonth(int year, int month) {
        return getTransactionsForPeriod("month", null, year, month, null);
    }

    @Override
    public List<Transaction> getTransactionsForCurrentMonth() {
        LocalDate now = LocalDate.now();
        return getTransactionsForMonth(now.getYear(), now.getMonthValue());
    }

    @Override
    public List<Transaction> getTransactionsForYear(int year) {
        return getTransactionsForPeriod("year", null, year, null, null);
    }

    @Override
    public List<Transaction> getTransactionsForCurrentYear() {
        return getTransactionsForYear(LocalDate.now().getYear());
    }

    @Override
    public List<Transaction> getTransactionsForDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return getTransactionsForPeriod("custom", startDate.toLocalDate(), null, null, endDate);
    }

    /**
     * New pagination-enabled methods
     */
    @Override
    public Page<Transaction> getTransactionsForPeriodPaginated(String periodType, LocalDate referenceDate,
                                                               Integer year, Integer month, LocalDateTime endDate,
                                                               Pageable pageable) {
        DateRangeResult dateRange = calculateDateRange(periodType, referenceDate, year, month, endDate);
        return transactionRepository.findByTransactionDateBetween(dateRange.startDateTime, dateRange.endDateTime, pageable);
    }

    @Override
    public Page<Transaction> getTransactionsForDayPaginated(LocalDate date, Pageable pageable) {
        return getTransactionsForPeriodPaginated("day", date, null, null, null, pageable);
    }

    @Override
    public Page<Transaction> getTransactionsForTodayPaginated(Pageable pageable) {
        return getTransactionsForDayPaginated(LocalDate.now(), pageable);
    }

    @Override
    public Page<Transaction> getTransactionsForWeekPaginated(LocalDate date, Pageable pageable) {
        return getTransactionsForPeriodPaginated("week", date, null, null, null, pageable);
    }

    @Override
    public Page<Transaction> getTransactionsForCurrentWeekPaginated(Pageable pageable) {
        return getTransactionsForWeekPaginated(LocalDate.now(), pageable);
    }

    @Override
    public Page<Transaction> getTransactionsForMonthPaginated(int year, int month, Pageable pageable) {
        return getTransactionsForPeriodPaginated("month", null, year, month, null, pageable);
    }

    @Override
    public Page<Transaction> getTransactionsForCurrentMonthPaginated(Pageable pageable) {
        LocalDate now = LocalDate.now();
        return getTransactionsForMonthPaginated(now.getYear(), now.getMonthValue(), pageable);
    }

    @Override
    public Page<Transaction> getTransactionsForYearPaginated(int year, Pageable pageable) {
        return getTransactionsForPeriodPaginated("year", null, year, null, null, pageable);
    }

    @Override
    public Page<Transaction> getTransactionsForCurrentYearPaginated(Pageable pageable) {
        return getTransactionsForYearPaginated(LocalDate.now().getYear(), pageable);
    }

    @Override
    public Page<Transaction> getTransactionsForDateRangePaginated(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return getTransactionsForPeriodPaginated("custom", startDate.toLocalDate(), null, null, endDate, pageable);
    }

    /**
     * Helper method to calculate date ranges based on period type
     */
    private DateRangeResult calculateDateRange(String periodType, LocalDate referenceDate,
                                               Integer year, Integer month, LocalDateTime endDate) {
        LocalDateTime startDateTime;
        LocalDateTime endDateTime;

        // If referenceDate is null, use current date
        if (referenceDate == null) {
            referenceDate = LocalDate.now();
        }

        switch (periodType.toLowerCase()) {
            case "day":
                // Start of day
                startDateTime = referenceDate.atTime(LocalTime.MIN);
                // End of day
                endDateTime = referenceDate.atTime(LocalTime.MAX);
                break;

            case "week":
                // Start of week (Monday)
                LocalDate startOfWeek = referenceDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                startDateTime = startOfWeek.atTime(LocalTime.MIN);
                // End of week (Sunday)
                LocalDate endOfWeek = startOfWeek.plusDays(6);
                endDateTime = endOfWeek.atTime(LocalTime.MAX);
                break;

            case "month":
                if (year != null && month != null) {
                    // Use provided year and month
                    LocalDate startOfMonth = LocalDate.of(year, month, 1);
                    startDateTime = startOfMonth.atTime(LocalTime.MIN);
                    LocalDate endOfMonth = startOfMonth.with(TemporalAdjusters.lastDayOfMonth());
                    endDateTime = endOfMonth.atTime(LocalTime.MAX);
                } else {
                    // Use reference date's year and month
                    LocalDate startOfMonth = referenceDate.withDayOfMonth(1);
                    startDateTime = startOfMonth.atTime(LocalTime.MIN);
                    LocalDate endOfMonth = referenceDate.with(TemporalAdjusters.lastDayOfMonth());
                    endDateTime = endOfMonth.atTime(LocalTime.MAX);
                }
                break;

            case "year":
                int yearValue = (year != null) ? year : referenceDate.getYear();
                // Start of year
                startDateTime = LocalDate.of(yearValue, 1, 1).atTime(LocalTime.MIN);
                // End of year
                endDateTime = LocalDate.of(yearValue, 12, 31).atTime(LocalTime.MAX);
                break;

            case "custom":
                // For custom period, use the provided dates
                startDateTime = referenceDate.atTime(LocalTime.MIN);
                endDateTime = (endDate != null) ? endDate : referenceDate.atTime(LocalTime.MAX);
                break;

            default:
                throw new IllegalArgumentException("Invalid period type: " + periodType);
        }

        return new DateRangeResult(startDateTime, endDateTime);
    }

    /**
     * Helper class to store calculation results
     */
    private static class DateRangeResult {
        private final LocalDateTime startDateTime;
        private final LocalDateTime endDateTime;

        public DateRangeResult(LocalDateTime startDateTime, LocalDateTime endDateTime) {
            this.startDateTime = startDateTime;
            this.endDateTime = endDateTime;
        }
    }



}
