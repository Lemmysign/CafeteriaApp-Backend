package Evercare_CafeteriaApp.Mapper;

import Evercare_CafeteriaApp.DTO.ServerDtoPackage.ServerDTO;
import Evercare_CafeteriaApp.DTO.ServerDtoPackage.ServerRegisterDTO;
import Evercare_CafeteriaApp.Model.Server;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ServerMapper {

    ServerMapper INSTANCE = Mappers.getMapper(ServerMapper.class);

    @Mapping(target = "roleName", source = "role.roleName")
    ServerDTO toDTO(Server server);

    @Mapping(target = "serverId", source = "serverId")
    @Mapping(target = "serverName", source = "serverName")
    @Mapping(target = "serverEmail", source = "serverEmail")
    @Mapping(target = "serverPassword", source = "serverPassword")
    @Mapping(target = "serverPhone", source = "serverPhone")
    @Mapping(target = "serverBlocked", constant = "false")
    @Mapping(target = "role", ignore = true)
    Server toEntity(ServerRegisterDTO serverRegisterDTO);
}