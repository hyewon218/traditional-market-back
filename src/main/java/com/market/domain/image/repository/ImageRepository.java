package com.market.domain.image.repository;

import com.market.domain.image.entity.Image;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
    List<Image> findByMarket_No(Long marketNo);
    Boolean existsByImageUrlAndNo(String fileUrl, Long no);
}