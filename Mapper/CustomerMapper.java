package Evercare_CafeteriaApp.Mapper;

import Evercare_CafeteriaApp.DTO.CustomerDtoPackage.CustomerDTO;
import Evercare_CafeteriaApp.DTO.CustomerDtoPackage.CustomerRegisterDTO;
import Evercare_CafeteriaApp.Model.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;


@Mapper(componentModel = "spring")
public interface CustomerMapper {

    CustomerMapper INSTANCE = Mappers.getMapper(CustomerMapper.class);

    @Mapping(target = "role", source = "role.roleName")
    @Mapping(target = "verified", source = "verified") //  Maps verified status
    @Mapping(target = "createdDate", source = "createdDate")
    @Mapping(target = "lastActionTime", source = "lastActionTime")
    CustomerDTO toDTO(Customer customer);


    @Mapping(target = "id", source = "id")
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "balance", constant = "0.0")
    @Mapping(target = "locked", constant = "true")  //  New customers are locked by default //  Fixed field name
    @Mapping(target = "blocked", constant = "false") //  Fixed field name
    @Mapping(target = "hideBalance", constant = "false")
    @Mapping(target = "verified", constant = "false")//  New customers are unverified by default
    @Mapping(target = "lastActionTime", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "orders", ignore = true) //  Ignore unmapped field
    @Mapping(target = "transactions", ignore = true)
    @Mapping(target = "createdDate", expression = "java(java.time.LocalDateTime.now())")
    Customer toEntity(CustomerRegisterDTO customerRegisterDTO);

}
