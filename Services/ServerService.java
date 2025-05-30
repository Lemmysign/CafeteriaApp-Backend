package Evercare_CafeteriaApp.Services;

import Evercare_CafeteriaApp.DTO.OrderDtoPackage.OrderDTO;
import Evercare_CafeteriaApp.DTO.ServerDtoPackage.DisplayServerNameIdDTO;
import Evercare_CafeteriaApp.Model.Server;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

public interface ServerService {

    Server login(String email, String password);

    void logout(HttpServletRequest request, HttpServletResponse response);

    DisplayServerNameIdDTO getCustomerBasicInfoByEmail(String email);

    Server findByEmail(String email);

    List<OrderDTO> getCustomerPendingOrders(Long customerId);
    OrderDTO serveCustomerOrder(Long orderId, Long customerId);
}


