package vn.edu.uit.is208.salon.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import vn.edu.uit.is208.salon.dto.CreateRecipeRequest;
import vn.edu.uit.is208.salon.dto.RecipeResponse;
import vn.edu.uit.is208.salon.service.RecipeService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/services/{serviceId}/recipes")
@RequiredArgsConstructor
public class RecipeController {
    private final RecipeService recipeService;

    @PostMapping
    public ResponseEntity<RecipeResponse> createRecipe(@PathVariable Long serviceId, @RequestBody @Valid CreateRecipeRequest request) {
        RecipeResponse createdServiceRecipe = recipeService.createRecipe(serviceId, request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .build()
                .toUri();

        return ResponseEntity.created(location).body(createdServiceRecipe);
    }

    @GetMapping
    public ResponseEntity<RecipeResponse> getRecipesByServiceId(@PathVariable Long serviceId) {
        return ResponseEntity.ok(recipeService.getRecipesByServiceId(serviceId));
    }
}