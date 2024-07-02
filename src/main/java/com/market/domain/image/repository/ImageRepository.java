package com.market.domain.image.repository;

import com.market.domain.image.entity.Image;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
    List<Image> findByMarket_No(Long marketNo);
    List<Image> findByShop_No(Long shopNo);
    List<Image> findByItem_No(Long item);
    Boolean existsByImageUrlAndNo(String fileUrl, Long no);
}