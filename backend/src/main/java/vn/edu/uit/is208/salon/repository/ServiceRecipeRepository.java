package vn.edu.uit.is208.salon.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.uit.is208.salon.entity.ServiceRecipe;
import vn.edu.uit.is208.salon.entity.ServiceRecipeId;

public interface ServiceRecipeRepository extends JpaRepository<ServiceRecipe, ServiceRecipeId> {
}
