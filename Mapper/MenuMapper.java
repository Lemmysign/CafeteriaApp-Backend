package Evercare_CafeteriaApp.Mapper;

import Evercare_CafeteriaApp.DTO.MenuDtoPackage.MenuCategoryDTO;
import Evercare_CafeteriaApp.DTO.MenuDtoPackage.MenuItemDTO;
import Evercare_CafeteriaApp.DTO.MenuDtoPackage.PageResponseDTO;
import Evercare_CafeteriaApp.Model.MenuCategory;
import Evercare_CafeteriaApp.Model.MenuItem;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class MenuMapper {

    public MenuItemDTO toMenuItemDTO(MenuItem menuItem) {
        MenuItemDTO dto = new MenuItemDTO();
        dto.setId(menuItem.getId());
        dto.setName(menuItem.getName());
        dto.setPrice(menuItem.getPrice());
        dto.setAvailable(menuItem.isAvailable());

        if (menuItem.getMenuCategory() != null) {
            dto.setCategoryId(menuItem.getMenuCategory().getId());
            dto.setCategoryName(menuItem.getMenuCategory().getName());
        }

        dto.setQuantity(1); // Default quantity

        return dto;
    }

    public MenuCategoryDTO toMenuCategoryDTO(MenuCategory category, List<MenuItem> menuItems) {
        MenuCategoryDTO dto = new MenuCategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());

        if (menuItems != null) {
            dto.setMenuItems(menuItems.stream()
                    .map(this::toMenuItemDTO)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    public <T> PageResponseDTO<T> toPageResponseDTO(Page<T> page) {
        PageResponseDTO<T> responseDTO = new PageResponseDTO<>();
        responseDTO.setContent(page.getContent());
        responseDTO.setPageNumber(page.getNumber());
        responseDTO.setPageSize(page.getSize());
        responseDTO.setTotalElements(page.getTotalElements());
        responseDTO.setTotalPages(page.getTotalPages());
        responseDTO.setLast(page.isLast());
        return responseDTO;
    }

    public PageResponseDTO<MenuItemDTO> toMenuItemPageDTO(Page<MenuItem> page) {
        Page<MenuItemDTO> mappedPage = page.map(this::toMenuItemDTO);
        return toPageResponseDTO(mappedPage);
    }
}