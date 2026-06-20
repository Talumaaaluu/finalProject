package com.example.foodvoting.controller;

import com.example.foodvoting.entity.Food;
import com.example.foodvoting.service.FoodService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class FoodController {
    private final FoodService foodService;

    @GetMapping("/")
    public String index(@RequestParam(required = false) String keyword, Model model) {
        List<Food> foods = foodService.searchFoods(keyword);
        Food winnerFood = foodService.getWinnerFood();
        model.addAttribute("foods", foods);
        model.addAttribute("winnerFood", winnerFood);
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        return "index";
    }

    @PostMapping("/food/add")
    public String addFood(@RequestParam String name,
                          @RequestParam MultipartFile image) {
        foodService.addFood(name, image);
        return "redirect:/";
    }

    @GetMapping("/food/edit/{id}")
    public String editFoodPage(@PathVariable Long id, Model model) {
        Food food = foodService.findFoodById(id);
        model.addAttribute("food", food);
        return "edit-food";
    }

    @PostMapping("/food/update")
    public String updateFood(@RequestParam Long id,
                             @RequestParam String name,
                             @RequestParam(required = false) MultipartFile image) {
        foodService.updateFood(id, name, image);
        return "redirect:/";
    }

    @PostMapping("/food/delete")
    public String deleteFood(@RequestParam Long id) {
        foodService.deleteFood(id);
        return "redirect:/";
    }

    @PostMapping("/food/vote")
    public String voteFood(@RequestParam Long id) {
        foodService.voteFood(id);
        return "redirect:/";
    }
}
