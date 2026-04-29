package vn.edu.uit.is208.salon.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.uit.is208.salon.entity.Recipe;
import java.util.List;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {
    List<Recipe> findByServiceId(Long serviceId);
}