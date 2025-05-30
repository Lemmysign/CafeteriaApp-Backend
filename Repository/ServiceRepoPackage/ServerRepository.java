package Evercare_CafeteriaApp.Repository.ServiceRepoPackage;

import Evercare_CafeteriaApp.DTO.CustomerDtoPackage.CustomerSummaryDTO;
import Evercare_CafeteriaApp.DTO.ServerDtoPackage.ServerSummaryDTO;
import Evercare_CafeteriaApp.Model.Order;
import Evercare_CafeteriaApp.Model.Server;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ServerRepository extends JpaRepository<Server, Long> {

    @Query("SELECT s FROM Server s WHERE LOWER(s.serverEmail) = LOWER(:serverEmail)")
    Optional<Server> findByEmailIgnoreCase(@Param("serverEmail") String email);
    //Find a Server by email (used for login & validation)

    boolean existsByServerEmail(String serverEmail);


    @Query("SELECT new Evercare_CafeteriaApp.DTO.ServerDtoPackage.ServerSummaryDTO(s.serverId, s.serverName, s.isServerBlocked) " +
            "FROM Server s WHERE " +
            "(:searchTerm IS NULL OR :searchTerm = '') OR " +
            "CAST(s.serverId AS string) = :searchTerm OR " +
            "LOWER(s.serverName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<ServerSummaryDTO> findServersByIdOrName(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Find all blocked servers
     * @return A list of ServerSummaryDTO objects for blocked servers
     */
    @Query("SELECT new Evercare_CafeteriaApp.DTO.ServerDtoPackage.ServerSummaryDTO(s.serverId, s.serverName, s.isServerBlocked) " +
            "FROM Server s WHERE s.isServerBlocked = true")
    List<ServerSummaryDTO> findAllBlockedServers();



}
