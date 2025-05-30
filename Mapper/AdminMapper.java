package Evercare_CafeteriaApp.Mapper;

import Evercare_CafeteriaApp.DTO.AdminDtoPackage.AdminDTO;
import Evercare_CafeteriaApp.Model.Admin;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


import java.util.List;

@Mapper(componentModel = "spring")
public interface AdminMapper {

    @Mapping(target = "roleName", source = "role.roleName") // Map Role entity to roleName
    AdminDTO fromAdmin(Admin admin);

    @Mapping(target = "role.roleName", source = "roleName") // Map roleName back to Role
    Admin fromAdminDTO(AdminDTO adminDTO);

    List<AdminDTO> fromAdmins(List<Admin> admins);
    List<Admin> fromAdminDTOs(List<AdminDTO> adminDTOs);
}
