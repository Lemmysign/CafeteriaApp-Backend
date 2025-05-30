package Evercare_CafeteriaApp.ServiceImplementation;

import Evercare_CafeteriaApp.Model.StaffMenuList;
import Evercare_CafeteriaApp.Repository.MenuRepoPackage.StaffMenuListRepository;
import Evercare_CafeteriaApp.Services.StaffMenuListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class StaffMenuListServiceImpl implements StaffMenuListService {

    private final StaffMenuListRepository staffMenuListRepository;

    @Autowired
    public StaffMenuListServiceImpl(StaffMenuListRepository staffMenuListRepository) {
        this.staffMenuListRepository = staffMenuListRepository;
    }

    /**
     * Get all foods with optimized loading for staff interface
     * Using caching to improve performance
     */
    @Override
    @Cacheable("allFoods")
    public List<StaffMenuList> getAllFoods() {
        return staffMenuListRepository.findAllWithCategoryAndType();
    }

    /**
     * Get only available foods for customer interface
     */
    @Override
    @Cacheable("availableFoods")
    public List<StaffMenuList> getAvailableFoods() {
        return staffMenuListRepository.findByIsAvailableTrue();
    }

    /**
     * Get a specific food by ID
     */
    @Override
    public Optional<StaffMenuList> getFoodById(Long id) {
        return staffMenuListRepository.findById(id);
    }

    /**
     * Get foods by category ID
     */
    @Override
    public List<StaffMenuList> getFoodsByCategory(Long categoryId) {
        return staffMenuListRepository.findByCategoryId(categoryId);
    }

    /**
     * Get foods by menu type ID
     */
    @Override
    public List<StaffMenuList> getFoodsByType(Long typeId) {
        return staffMenuListRepository.findByTypeId(typeId);
    }

    /**
     * Toggle food availability status
     */
    @Override
    @Transactional
    @CacheEvict(cacheNames = {"allFoods", "availableFoods"}, allEntries = true)
    public StaffMenuList toggleFoodAvailability(Long foodId) {
        Optional<StaffMenuList> foodOptional = staffMenuListRepository.findById(foodId);

        if (foodOptional.isPresent()) {
            StaffMenuList food = foodOptional.get();
            food.setAvailable(!food.isAvailable());
            return staffMenuListRepository.save(food);
        } else {
            throw new RuntimeException("Food not found with id: " + foodId);
        }
    }

    /**
     * Update food availability to a specific state
     */
    @Override
    @Transactional
    @CacheEvict(cacheNames = {"allFoods", "availableFoods"}, allEntries = true)
    public StaffMenuList updateFoodAvailability(Long foodId, boolean isAvailable) {
        Optional<StaffMenuList> foodOptional = staffMenuListRepository.findById(foodId);

        if (foodOptional.isPresent()) {
            StaffMenuList food = foodOptional.get();
            food.setAvailable(isAvailable);
            return staffMenuListRepository.save(food);
        } else {
            throw new RuntimeException("Food not found with id: " + foodId);
        }
    }

    /**
     * Batch update food availability
     */
    @Override
    @Transactional
    @CacheEvict(cacheNames = {"allFoods", "availableFoods"}, allEntries = true)
    public void batchUpdateAvailability(List<Long> foodIds, boolean isAvailable) {
        for (Long id : foodIds) {
            Optional<StaffMenuList> foodOptional = staffMenuListRepository.findById(id);
            foodOptional.ifPresent(food -> {
                food.setAvailable(isAvailable);
                staffMenuListRepository.save(food);
            });
        }
    }
}