package vn.edu.uit.is208.salon.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.uit.is208.salon.entity.Product;
import vn.edu.uit.is208.salon.projection.ProductSummary;

import java.util.Collection;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    @Query("SELECT p.id as id, p.price as price, p.conversionFactor as conversionFactor, p.productType as productType FROM Product p WHERE p.id IN :ids")
    List<ProductSummary> getProductSummaryByIds(@Param("ids") Collection<Long> ids);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id IN :ids ORDER BY p.id ASC")
    List<Product> findByIdsWithLock(@Param("ids") Collection<Long> ids);

    boolean existsByNameIgnoreCase(String name);
}
