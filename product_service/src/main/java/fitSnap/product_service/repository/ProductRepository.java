package fitSnap.product_service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fitSnap.product_service.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {
    List<Product> findByUserId(String userId);
}
