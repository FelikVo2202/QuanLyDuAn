package vn.edu.uit.is208.salon.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.uit.is208.salon.entity.ServiceRecipe;
import vn.edu.uit.is208.salon.entity.ServiceRecipeId;

import java.util.Collection;
import java.util.List;

public interface ServiceRecipeRepository extends JpaRepository<ServiceRecipe, ServiceRecipeId> {
    List<ServiceRecipe> findByService_IdIn(Collection<Long> serviceIds);

    boolean existsByProductId(Long id);

    @EntityGraph(attributePaths = {"service", "product"})
    List<ServiceRecipe> findByServiceId(Long serviceId);

    boolean existsByIdServiceIdAndIdProductId(Long serviceId, Long productId);
}