package vn.edu.uit.is208.salon.repository.specifications;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import vn.edu.uit.is208.salon.constant.ProductType;
import vn.edu.uit.is208.salon.entity.Product;

public final class ProductSpecification {
    public static Specification<Product> withFilters(ProductType productType, String category, String search) {
        return Specification
                .where(hasProductType(productType))
                .and(hasCategory(category))
                .and(nameContainsIgnoreCase(search));
    }

    public static Specification<Product> hasProductType(ProductType productType) {
        return (root, query, cb) -> {
            if (productType == null) return null;
            return cb.equal(root.get("productType"), productType);
        };
    }

    public static Specification<Product> hasCategory(String category) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(category)) return null;
            return cb.equal(root.get("category"), category);
        };
    }

    public static Specification<Product> nameContainsIgnoreCase(String search) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(search)) return null;
            String pattern = "%" + search.toLowerCase() + "%";
            return cb.like(cb.lower(root.get("name")), pattern);
        };
    }
}
