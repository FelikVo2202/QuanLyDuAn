package vn.edu.uit.is208.salon.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.edu.uit.is208.salon.dto.CreateRecipeRequest;
import vn.edu.uit.is208.salon.dto.RecipeResponse;
import vn.edu.uit.is208.salon.service.RecipeService;

import java.util.List;

@RestController
@RequestMapping("/api/recipes")
@RequiredArgsConstructor
public class RecipeController {
    private final RecipeService recipeService;

    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<RecipeResponse> createRecipe(@RequestBody CreateRecipeRequest request) {
        return ResponseEntity.ok(recipeService.createRecipe(request));
    }

    @GetMapping("/service/{serviceId}")
    public ResponseEntity<List<RecipeResponse>> getRecipesByService(@PathVariable Long serviceId) {
        return ResponseEntity.ok(recipeService.getRecipesByService(serviceId));
    }
}