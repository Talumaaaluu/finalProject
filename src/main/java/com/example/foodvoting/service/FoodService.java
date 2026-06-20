package com.example.foodvoting.service;

import com.example.foodvoting.entity.Food;
import com.example.foodvoting.repository.FoodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FoodService {
    private final FoodRepository foodRepository;
    private final Path uploadDir = Paths.get("uploads").toAbsolutePath().normalize();

    public List<Food> findAllFoods() {
        return foodRepository.findAll();
    }

    public List<Food> searchFoods(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return findAllFoods();
        }
        return foodRepository.findByNameContainingIgnoreCase(keyword.trim());
    }

    public Food findFoodById(Long id) {
        return foodRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Food not found."));
    }

    public void addFood(String name, MultipartFile imageFile) {
        String fileName = saveImage(imageFile);
        Food food = Food.builder()
                .name(name)
                .voteCount(0)
                .imageName(fileName)
                .build();
        foodRepository.save(food);
    }

    public void updateFood(Long id, String name, MultipartFile imageFile) {
        Food food = findFoodById(id);
        food.setName(name);

        if (imageFile != null && !imageFile.isEmpty()) {
            deleteImageFile(food.getImageName());
            food.setImageName(saveImage(imageFile));
        }

        foodRepository.save(food);
    }

    public void deleteFood(Long id) {
        Food food = findFoodById(id);
        deleteImageFile(food.getImageName());
        foodRepository.delete(food);
    }

    public void voteFood(Long id) {
        Food food = findFoodById(id);
        food.setVoteCount(food.getVoteCount() + 1);
        foodRepository.save(food);
    }

    public Food getWinnerFood() {
        List<Food> foods = foodRepository.findAll();
        if (foods.isEmpty()) {
            return null;
        }
        return foods.stream()
                .max((f1, f2) -> Integer.compare(f1.getVoteCount(), f2.getVoteCount()))
                .orElse(null);
    }

    private String saveImage(MultipartFile imageFile) {
        if (imageFile == null || imageFile.isEmpty()) {
            throw new IllegalArgumentException("Image is required.");
        }

        try {
            Files.createDirectories(uploadDir);
            String originalName = imageFile.getOriginalFilename();
            String safeName = (originalName == null || originalName.isBlank()) ? "image" : originalName;
            String fileName = UUID.randomUUID() + "_" + safeName.replaceAll("[^a-zA-Z0-9._-]", "_");
            Path destination = uploadDir.resolve(fileName);
            imageFile.transferTo(destination);
            return fileName;
        } catch (IOException e) {
            throw new RuntimeException("Image upload failed.", e);
        }
    }

    private void deleteImageFile(String imageName) {
        if (imageName == null || imageName.isBlank()) {
            return;
        }
        try {
            Files.deleteIfExists(uploadDir.resolve(imageName));
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete image file.", e);
        }
    }
}
