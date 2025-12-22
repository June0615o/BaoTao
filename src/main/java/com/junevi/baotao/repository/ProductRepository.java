package com.junevi.baotao.repository;

import com.junevi.baotao.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    /**
     * 原子扣减库存：仅当 stock >= qty 时才会扣减，避免并发下超卖
     *
     * @return 影响行数；1 表示扣减成功，0 表示库存不足或商品不存在
     */
    @Modifying
    @Query("update Product p set p.stock = p.stock - :qty where p.id = :productId and p.stock >= :qty")
    int decreaseStockIfEnough(@Param("productId") Long productId, @Param("qty") int qty);
}


