package Evercare_CafeteriaApp.Services;

import Evercare_CafeteriaApp.Model.StaffMenuList;

import java.util.List;
import java.util.Optional;

public interface StaffMenuListService {

    /**
     * Get all foods
     */
    List<StaffMenuList> getAllFoods();

    /**
     * Get only available foods for customer interface
     */
    List<StaffMenuList> getAvailableFoods();

    /**
     * Get a specific food by ID
     */
    Optional<StaffMenuList> getFoodById(Long id);

    /**
     * Get foods by category ID
     */
    List<StaffMenuList> getFoodsByCategory(Long categoryId);

    /**
     * Get foods by menu type ID
     */
    List<StaffMenuList> getFoodsByType(Long typeId);

    /**
     * Toggle food availability status
     */
    StaffMenuList toggleFoodAvailability(Long foodId);

    /**
     * Update food availability to a specific state
     */
    StaffMenuList updateFoodAvailability(Long foodId, boolean isAvailable);

    /**
     * Batch update food availability
     */
    void batchUpdateAvailability(List<Long> foodIds, boolean isAvailable);
}