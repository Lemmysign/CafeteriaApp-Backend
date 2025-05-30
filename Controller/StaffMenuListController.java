package Evercare_CafeteriaApp.Controller;

import Evercare_CafeteriaApp.Model.StaffMenuList;
import Evercare_CafeteriaApp.Services.StaffMenuListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/menu")
// Allow requests from your React frontend
public class StaffMenuListController {

    private final StaffMenuListService staffMenuListService;

    @Autowired
    public StaffMenuListController(StaffMenuListService staffMenuListService) {
        this.staffMenuListService = staffMenuListService;
    }

    /**
     * Get all menu items (for staff interface)
     * @return List of all menu items
     */
    @GetMapping("/all")
    public ResponseEntity<List<StaffMenuList>> getAllFoods() {
        return ResponseEntity.ok(staffMenuListService.getAllFoods());
    }

    /**
     * Get only available menu items (for customer interface)
     * @return List of available menu items
     */
    @GetMapping("/available")
    public ResponseEntity<List<StaffMenuList>> getAvailableFoods() {
        return ResponseEntity.ok(staffMenuListService.getAvailableFoods());
    }

    /**
     * Get a menu item by ID
     * @param id The ID of the menu item
     * @return The menu item or 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<StaffMenuList> getFoodById(@PathVariable Long id) {
        Optional<StaffMenuList> food = staffMenuListService.getFoodById(id);
        return food.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Get menu items by category
     * @param categoryId The category ID
     * @return List of menu items in the specified category
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<StaffMenuList>> getFoodsByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(staffMenuListService.getFoodsByCategory(categoryId));
    }



    /**
     * Toggle availability status of a menu item
     * @param id The ID of the menu item
     * @return The updated menu item
     */
    @PutMapping("/{id}/toggle-availability")
    public ResponseEntity<StaffMenuList> toggleFoodAvailability(@PathVariable Long id) {
        try {
            StaffMenuList updatedFood = staffMenuListService.toggleFoodAvailability(id);
            return ResponseEntity.ok(updatedFood);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Update availability status of a menu item
     * @param id The ID of the menu item
     * @param requestBody JSON object containing isAvailable boolean
     * @return The updated menu item
     */
    @PutMapping("/{id}/availability")
    public ResponseEntity<StaffMenuList> updateFoodAvailability(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> requestBody) {

        Boolean isAvailable = requestBody.get("isAvailable");
        if (isAvailable == null) {
            return ResponseEntity.badRequest().build();
        }

        try {
            StaffMenuList updatedFood = staffMenuListService.updateFoodAvailability(id, isAvailable);
            return ResponseEntity.ok(updatedFood);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }


    }

    /**
     * Batch update availability status for multiple menu items
     * @param requestBody JSON object containing foodIds array and isAvailable boolean
     * @return Success status
     */
    @PutMapping("/batch-availability")
    public ResponseEntity<String> batchUpdateAvailability(@RequestBody Map<String, Object> requestBody) {
        try {
            @SuppressWarnings("unchecked")
            List<Long> foodIds = (List<Long>) requestBody.get("foodIds");
            Boolean isAvailable = (Boolean) requestBody.get("isAvailable");

            if (foodIds == null || isAvailable == null) {
                return ResponseEntity.badRequest().body("Missing required fields: foodIds or isAvailable");
            }

            staffMenuListService.batchUpdateAvailability(foodIds, isAvailable);
            return ResponseEntity.ok("Batch update successful");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing batch update: " + e.getMessage());
        }
    }
}