package Evercare_CafeteriaApp.Services;

import Evercare_CafeteriaApp.DTO.CustomerDtoPackage.CustomerRegisterDTO;
import Evercare_CafeteriaApp.DTO.CustomerDtoPackage.DisplayUserNameIdDTO;
import Evercare_CafeteriaApp.Model.Customer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.math.BigDecimal;

public interface CustomerService {

    Customer login(String email, String password);

    Customer register(CustomerRegisterDTO customerRegisterDTO);

    void logout(HttpServletRequest request, HttpServletResponse response);


    //to unlock/lock account
    Boolean toggleAccountLock(Long id);

    //to check locked status
    Boolean isAccountLocked(Long id);

    String transferCredit(Long receiverId, BigDecimal amount, HttpServletRequest request);

    void sendTransferEmails(Customer sender, Customer receiver, BigDecimal amount);

    Customer findByEmail(String email);

    DisplayUserNameIdDTO getCustomerBasicInfoByEmail(String email);



    //used for validating receiver before transfer
    String getReceiverName(Long receiverId);

    //to auto-lock account after some time
    void autoLockCustomersAfterFiveMinutes();

}
