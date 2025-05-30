package Evercare_CafeteriaApp.Controller;
import Evercare_CafeteriaApp.DTO.CustomerDtoPackage.CustomerSummaryDTO;
import Evercare_CafeteriaApp.DTO.CustomerDtoPackage.PagedResponse;
import Evercare_CafeteriaApp.DTO.PaymentDtoPackage.TransactionDTO;
import Evercare_CafeteriaApp.DTO.ServerDtoPackage.ServerRegisterDTO;
import Evercare_CafeteriaApp.DTO.ServerDtoPackage.ServerSummaryDTO;
import Evercare_CafeteriaApp.Mapper.TransactionMapper;
import Evercare_CafeteriaApp.Model.*;
import Evercare_CafeteriaApp.ServiceImplementation.CSVGenerator;
import Evercare_CafeteriaApp.Services.AdminService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



@RestController
@RequestMapping ("/api/admin")
public class AdminRestController {

    private final AdminService adminService;
    private final TransactionMapper transactionMapper;
    private final CSVGenerator csvGenerator;

    public AdminRestController(AdminService adminService, TransactionMapper transactionMapper, CSVGenerator csvGenerator) {
        this.adminService = adminService;

        this.transactionMapper = transactionMapper;
        this.csvGenerator = csvGenerator;
    }

    @PostMapping("/adminLogin")
    public ResponseEntity<Map<String, Object>> login(
            @RequestParam String email,
            @RequestParam String password,
            HttpServletRequest request) {
        try {
            // Invalidate previous session
            HttpSession oldSession = request.getSession(false);
            if (oldSession != null) {
                oldSession.invalidate();
            }
            // Authenticate user
            Admin admin = adminService.login(email, password);
            // Create new session and store user info
            HttpSession newSession = request.getSession(true);
            newSession.setAttribute("adminId", admin.getAdminId());
            newSession.setAttribute("adminEmail", admin.getAdminEmail());

            // Prepare response (no need to send session token)
            Map<String, Object> response = new HashMap<>();
            response.put("name", admin.getAdminName());
            response.put("id", admin.getAdminId());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }



    @GetMapping("/adminDetails")
    public ResponseEntity<?> getCurrentAdmin(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("adminEmail") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not logged in");
        }

        String email = (String) session.getAttribute("adminEmail");
        String sessionToken = (String) session.getAttribute("sessionToken");

        try {
            Admin admin = adminService.findByEmail(email);
            Map<String, Object> response = new HashMap<>();
            response.put("id", admin.getAdminId());
            response.put("name", admin.getAdminName());


            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }



    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate(); // Destroy session
        }

        response.setHeader("Set-Cookie", "JSESSIONID=; Path=/; HttpOnly; Max-Age=0");

        return ResponseEntity.ok("Logged out successfully.");
    }


    @GetMapping("/totalCustomerCount")
    public ResponseEntity<Long> getTotalCustomerCount() {
        long count = adminService.getTotalCustomerCount();
        return ResponseEntity.ok(count);
    }


    @GetMapping("/transaction-daily-count")
    public long getTransactionCountForDate(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        if (date != null) {
            return adminService.getTransactionCountForDate(date);
        } else {
            return adminService.getTransactionCountForToday();
        }
    }


    @PostMapping("/registerServer")
    public ResponseEntity<String> registerCustomer(@RequestBody ServerRegisterDTO serverRegisterDTO, HttpServletRequest request) {
        Server registeredServer = adminService.registerServer(serverRegisterDTO);

        // Invalidate old session (if any)
        HttpSession existingSession = request.getSession(false);
        if (existingSession != null) {
            existingSession.invalidate();
        }

        // Create a new session for the registered user
        HttpSession session = request.getSession(true);
        session.setAttribute("serverEmail", registeredServer.getServerEmail());

        return ResponseEntity.ok("Server registered successfully: " + registeredServer.getServerEmail());
    }


    @PutMapping("/{id}/toggle-block-staff")
    public ResponseEntity<Map<String, Object>> toggleCustomerBlock(@PathVariable Long id) {
        boolean isBlocked = adminService.toggleStaffAccountBlock(id);

        Map<String, Object> response = new HashMap<>();
        response.put("isBlocked", isBlocked);
        response.put("message", isBlocked ? "account blocked" : "account unblocked");

        return ResponseEntity.ok(response);
    }


    @PutMapping("/{id}/toggle-block-server")
    public ResponseEntity<Map<String, Object>>toggleServersAccountBlock(@PathVariable Long id) {
        boolean isBlocked = adminService.toggleServersAccountBlock(id);

        Map<String, Object> response = new HashMap<>();
        response.put("isBlocked", isBlocked);
        response.put("message", isBlocked ? "account blocked" : "account unblocked");
        return ResponseEntity.ok(response);
    }


    @GetMapping("/staffSearch")
    public ResponseEntity<PagedResponse<CustomerSummaryDTO>> searchCustomers(
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction) {

        Sort.Direction sortDirection = Sort.Direction.fromString(direction.toUpperCase());
        Pageable pageable = PageRequest.of(page, size, sortDirection, sortBy);

        Page<CustomerSummaryDTO> customersPage = adminService.findCustomersByIdOrName(search, pageable);

        // Convert Page to our custom PagedResponse
        PagedResponse<CustomerSummaryDTO> response = new PagedResponse<>(
                customersPage.getContent(),
                customersPage.getNumber(),
                customersPage.getSize(),
                customersPage.getTotalElements(),
                customersPage.getTotalPages(),
                customersPage.isLast(),
                customersPage.isFirst()
        );
        return ResponseEntity.ok(response);
    }


    @GetMapping("/findBlockedStaff")
    public ResponseEntity<List<CustomerSummaryDTO>> getAllBlockedCustomers() {
        List<CustomerSummaryDTO> blockedCustomers = adminService.getAllBlockedCustomers();
        return ResponseEntity.ok(blockedCustomers);
    }

    @GetMapping("/serverSearch")
    public ResponseEntity<PagedResponse<ServerSummaryDTO>> searchServers(
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction) {

        // Map the sortBy field from DTO name to entity name
        String entitySortBy = mapSortProperty(sortBy);

        Sort.Direction sortDirection = Sort.Direction.fromString(direction.toUpperCase());
        Pageable pageable = PageRequest.of(page, size, sortDirection, entitySortBy);

        Page<ServerSummaryDTO> serversPage = adminService.findServersByIdOrName(search, pageable);

        // Convert Page to our custom PagedResponse
        PagedResponse<ServerSummaryDTO> response = new PagedResponse<>(
                serversPage.getContent(),
                serversPage.getNumber(),
                serversPage.getSize(),
                serversPage.getTotalElements(),
                serversPage.getTotalPages(),
                serversPage.isLast(),
                serversPage.isFirst()
        );
        return ResponseEntity.ok(response);
    }



    private String mapSortProperty(String sortBy) {
        if (sortBy == null) {
            return "serverId"; // Default sort
        }

        return switch (sortBy.toLowerCase()) {
            case "id" -> "serverId";
            case "name" -> "serverName";
            case "blocked" -> "isServerBlocked";
            default -> "serverId";  // Default sort
        };
    }


    @GetMapping("/findBlockedServers")
    public ResponseEntity<List<ServerSummaryDTO>> getAllBlockedServers() {
        List<ServerSummaryDTO> blockedServers = adminService.getAllBlockedServers();
        return ResponseEntity.ok(blockedServers);
    }


    static class PaginatedResponse<T> {
        private final List<T> content;
        private final int page;
        private final int size;
        private final long totalElements;
        private final int totalPages;

        public PaginatedResponse(Page<T> page) {
            this.content = page.getContent();
            this.page = page.getNumber();
            this.size = page.getSize();
            this.totalElements = page.getTotalElements();
            this.totalPages = page.getTotalPages();
        }

        // Getters
        public List<T> getContent() { return content; }
        public int getPage() { return page; }
        public int getSize() { return size; }
        public long getTotalElements() { return totalElements; }
        public int getTotalPages() { return totalPages; }
    }

    /**
     * TRANSACTION ENDPOINTS WITH PAGINATION
     */

    // GET transactions for today with pagination
    @GetMapping("/daily")
    public ResponseEntity<?> getDailyTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "false") boolean paginated) {

        if (paginated) {
            Pageable pageable = PageRequest.of(page, size);
            Page<Transaction> transactionPage = adminService.getTransactionsForTodayPaginated(pageable);
            Page<TransactionDTO> dtoPage = transactionPage.map(transactionMapper::fromTransaction);
            return ResponseEntity.ok(new PaginatedResponse<>(dtoPage));
        } else {
            // Return all results for backward compatibility
            List<Transaction> transactions = adminService.getTransactionsForToday();
            return ResponseEntity.ok(transactionMapper.fromTransactions(transactions));
        }
    }

    // GET transactions for a specific day with pagination
    @GetMapping("/daily/{date}")
    public ResponseEntity<?> getDailyTransactions(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "false") boolean paginated) {

        if (paginated) {
            Pageable pageable = PageRequest.of(page, size);
            Page<Transaction> transactionPage = adminService.getTransactionsForDayPaginated(date, pageable);
            Page<TransactionDTO> dtoPage = transactionPage.map(transactionMapper::fromTransaction);
            return ResponseEntity.ok(new PaginatedResponse<>(dtoPage));
        } else {
            List<Transaction> transactions = adminService.getTransactionsForDay(date);
            return ResponseEntity.ok(transactionMapper.fromTransactions(transactions));
        }
    }

    // GET transactions for current week with pagination
    @GetMapping("/weekly")
    public ResponseEntity<?> getWeeklyTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "false") boolean paginated) {

        if (paginated) {
            Pageable pageable = PageRequest.of(page, size);
            Page<Transaction> transactionPage = adminService.getTransactionsForCurrentWeekPaginated(pageable);
            Page<TransactionDTO> dtoPage = transactionPage.map(transactionMapper::fromTransaction);
            return ResponseEntity.ok(new PaginatedResponse<>(dtoPage));
        } else {
            List<Transaction> transactions = adminService.getTransactionsForCurrentWeek();
            return ResponseEntity.ok(transactionMapper.fromTransactions(transactions));
        }
    }

    // GET transactions for a week containing the specified date with pagination
    @GetMapping("/weekly/{date}")
    public ResponseEntity<?> getWeeklyTransactions(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "false") boolean paginated) {

        if (paginated) {
            Pageable pageable = PageRequest.of(page, size);
            Page<Transaction> transactionPage = adminService.getTransactionsForWeekPaginated(date, pageable);
            Page<TransactionDTO> dtoPage = transactionPage.map(transactionMapper::fromTransaction);
            return ResponseEntity.ok(new PaginatedResponse<>(dtoPage));
        } else {
            List<Transaction> transactions = adminService.getTransactionsForWeek(date);
            return ResponseEntity.ok(transactionMapper.fromTransactions(transactions));
        }
    }

    // GET transactions for current month with pagination
    @GetMapping("/monthly")
    public ResponseEntity<?> getMonthlyTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "false") boolean paginated) {

        if (paginated) {
            Pageable pageable = PageRequest.of(page, size);
            Page<Transaction> transactionPage = adminService.getTransactionsForCurrentMonthPaginated(pageable);
            Page<TransactionDTO> dtoPage = transactionPage.map(transactionMapper::fromTransaction);
            return ResponseEntity.ok(new PaginatedResponse<>(dtoPage));
        } else {
            List<Transaction> transactions = adminService.getTransactionsForCurrentMonth();
            return ResponseEntity.ok(transactionMapper.fromTransactions(transactions));
        }
    }

    // GET transactions for a specific month with pagination
    @GetMapping("/monthly/{year}/{month}")
    public ResponseEntity<?> getMonthlyTransactions(
            @PathVariable int year,
            @PathVariable int month,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "false") boolean paginated) {

        if (paginated) {
            Pageable pageable = PageRequest.of(page, size);
            Page<Transaction> transactionPage = adminService.getTransactionsForMonthPaginated(year, month, pageable);
            Page<TransactionDTO> dtoPage = transactionPage.map(transactionMapper::fromTransaction);
            return ResponseEntity.ok(new PaginatedResponse<>(dtoPage));
        } else {
            List<Transaction> transactions = adminService.getTransactionsForMonth(year, month);
            return ResponseEntity.ok(transactionMapper.fromTransactions(transactions));
        }
    }

    // GET transactions for current year with pagination
    @GetMapping("/yearly")
    public ResponseEntity<?> getYearlyTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "false") boolean paginated) {

        if (paginated) {
            Pageable pageable = PageRequest.of(page, size);
            Page<Transaction> transactionPage = adminService.getTransactionsForCurrentYearPaginated(pageable);
            Page<TransactionDTO> dtoPage = transactionPage.map(transactionMapper::fromTransaction);
            return ResponseEntity.ok(new PaginatedResponse<>(dtoPage));
        } else {
            List<Transaction> transactions = adminService.getTransactionsForCurrentYear();
            return ResponseEntity.ok(transactionMapper.fromTransactions(transactions));
        }
    }

    // GET transactions for a specific year with pagination
    @GetMapping("/yearly/{year}")
    public ResponseEntity<?> getYearlyTransactions(
            @PathVariable int year,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "false") boolean paginated) {

        if (paginated) {
            Pageable pageable = PageRequest.of(page, size);
            Page<Transaction> transactionPage = adminService.getTransactionsForYearPaginated(year, pageable);
            Page<TransactionDTO> dtoPage = transactionPage.map(transactionMapper::fromTransaction);
            return ResponseEntity.ok(new PaginatedResponse<>(dtoPage));
        } else {
            List<Transaction> transactions = adminService.getTransactionsForYear(year);
            return ResponseEntity.ok(transactionMapper.fromTransactions(transactions));
        }
    }

    // GET transactions for a custom date range with pagination
    @GetMapping("/range")
    public ResponseEntity<?> getTransactionsForDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "false") boolean paginated) {

        if (paginated) {
            Pageable pageable = PageRequest.of(page, size);
            Page<Transaction> transactionPage = adminService.getTransactionsForDateRangePaginated(startDate, endDate, pageable);
            Page<TransactionDTO> dtoPage = transactionPage.map(transactionMapper::fromTransaction);
            return ResponseEntity.ok(new PaginatedResponse<>(dtoPage));
        } else {
            List<Transaction> transactions = adminService.getTransactionsForDateRange(startDate, endDate);
            return ResponseEntity.ok(transactionMapper.fromTransactions(transactions));
        }
    }

    /**
     * DOWNLOADING TRANSACTIONS ENDPOINTS
     * Note: Downloads always fetch all data (no pagination)
     */

    // Download transactions for today
    @GetMapping("/download/daily")
    public ResponseEntity<byte[]> downloadDailyTransactions() {
        LocalDate today = LocalDate.now();
        List<Transaction> transactions = adminService.getTransactionsForToday();
        List<TransactionDTO> transactionDTOs = transactionMapper.fromTransactions(transactions);
        return generateCSVResponse(transactionDTOs, generateFilename("daily", today, null, null, null));
    }

    // Download transactions for a specific day
    @GetMapping("/download/daily/{date}")
    public ResponseEntity<byte[]> downloadDailyTransactions(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<Transaction> transactions = adminService.getTransactionsForDay(date);
        List<TransactionDTO> transactionDTOs = transactionMapper.fromTransactions(transactions);
        return generateCSVResponse(transactionDTOs, generateFilename("daily", date, null, null, null));
    }

    // Download transactions for current week
    @GetMapping("/download/weekly")
    public ResponseEntity<byte[]> downloadWeeklyTransactions() {
        LocalDate today = LocalDate.now();
        List<Transaction> transactions = adminService.getTransactionsForCurrentWeek();
        List<TransactionDTO> transactionDTOs = transactionMapper.fromTransactions(transactions);
        return generateCSVResponse(transactionDTOs, generateFilename("weekly", today, null, null, null));
    }

    // Download transactions for a week containing the specified date
    @GetMapping("/download/weekly/{date}")
    public ResponseEntity<byte[]> downloadWeeklyTransactions(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<Transaction> transactions = adminService.getTransactionsForWeek(date);
        List<TransactionDTO> transactionDTOs = transactionMapper.fromTransactions(transactions);
        return generateCSVResponse(transactionDTOs, generateFilename("weekly", date, null, null, null));
    }

    // Download transactions for current month
    @GetMapping("/download/monthly")
    public ResponseEntity<byte[]> downloadMonthlyTransactions() {
        LocalDate today = LocalDate.now();
        List<Transaction> transactions = adminService.getTransactionsForCurrentMonth();
        List<TransactionDTO> transactionDTOs = transactionMapper.fromTransactions(transactions);
        return generateCSVResponse(transactionDTOs, generateFilename("monthly", today, null, null, null));
    }

    // Download transactions for a specific month
    @GetMapping("/download/monthly/{year}/{month}")
    public ResponseEntity<byte[]> downloadMonthlyTransactions(
            @PathVariable int year, @PathVariable int month) {
        List<Transaction> transactions = adminService.getTransactionsForMonth(year, month);
        List<TransactionDTO> transactionDTOs = transactionMapper.fromTransactions(transactions);
        return generateCSVResponse(transactionDTOs, generateFilename("monthly", null, year, month, null));
    }

    // Download transactions for current year
    @GetMapping("/download/yearly")
    public ResponseEntity<byte[]> downloadYearlyTransactions() {
        LocalDate today = LocalDate.now();
        List<Transaction> transactions = adminService.getTransactionsForCurrentYear();
        List<TransactionDTO> transactionDTOs = transactionMapper.fromTransactions(transactions);
        return generateCSVResponse(transactionDTOs, generateFilename("yearly", today, null, null, null));
    }

    // Download transactions for a specific year
    @GetMapping("/download/yearly/{year}")
    public ResponseEntity<byte[]> downloadYearlyTransactions(@PathVariable int year) {
        List<Transaction> transactions = adminService.getTransactionsForYear(year);
        List<TransactionDTO> transactionDTOs = transactionMapper.fromTransactions(transactions);
        return generateCSVResponse(transactionDTOs, generateFilename("yearly", null, year, null, null));
    }

    // Download transactions for a custom date range
    @GetMapping("/download/range")
    public ResponseEntity<byte[]> downloadTransactionsForDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<Transaction> transactions = adminService.getTransactionsForDateRange(startDate, endDate);
        List<TransactionDTO> transactionDTOs = transactionMapper.fromTransactions(transactions);
        return generateCSVResponse(transactionDTOs, generateFilename("custom", startDate.toLocalDate(), null, null, endDate));
    }

    /**
     * Helper methods for generating CSV responses
     */

    private ResponseEntity<byte[]> generateCSVResponse(List<TransactionDTO> transactions, String filename) {
        byte[] csvData = csvGenerator.generateTransactionCSV(transactions);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", filename + ".csv");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return ResponseEntity.ok()
                .headers(headers)
                .body(csvData);
    }

    private String generateFilename(String periodType, LocalDate date, Integer year, Integer month, LocalDateTime endDate) {
        // Kept the same as in your original code
        StringBuilder filename = new StringBuilder("transactions_");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        switch (periodType) {
            case "daily":
                filename.append("daily_").append(date.format(formatter));
                break;
            case "weekly":
                LocalDate weekStart = date.minusDays(date.getDayOfWeek().getValue() - 1);
                LocalDate weekEnd = weekStart.plusDays(6);
                filename.append("weekly_").append(weekStart.format(formatter))
                        .append("_to_").append(weekEnd.format(formatter));
                break;
            case "monthly":
                if (year != null && month != null) {
                    filename.append("monthly_").append(year).append("_").append(String.format("%02d", month));
                } else {
                    filename.append("monthly_").append(date.getYear()).append("_").append(String.format("%02d", date.getMonthValue()));
                }
                break;
            case "yearly":
                if (year != null) {
                    filename.append("yearly_").append(year);
                } else {
                    filename.append("yearly_").append(date.getYear());
                }
                break;
            case "custom":
                filename.append("custom_").append(date.format(formatter));
                if (endDate != null) {
                    filename.append("_to_").append(endDate.toLocalDate().format(formatter));
                }
                break;
            default:
                filename.append(periodType).append("_").append(LocalDate.now().format(formatter));
        }

        return filename.toString();
    }

}
