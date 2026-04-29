package vn.edu.uit.is208.salon.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.uit.is208.salon.entity.ServiceRecipe;
import java.util.List;

public interface RecipeRepository extends JpaRepository<ServiceRecipe, Long> {
    List<ServiceRecipe> findByServiceId(Long serviceId);
}