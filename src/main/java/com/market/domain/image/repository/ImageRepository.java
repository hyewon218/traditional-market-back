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

    Boolean existsByImageUrlAndMarket_No(String imageUrl, Long marketNo);

    Boolean existsByImageUrlAndShop_No(String imageUrl, Long shopNo);

    Boolean existsByImageUrlAndItem_No(String imageUrl, Long no);

    void deleteByImageUrlAndMarket_No(String defaultImageUrl, Long marketNo);

    void deleteByImageUrlAndShop_No(String defaultImageUrl, Long shopNo);

    void deleteByImageUrlAndItem_No(String defaultImageUrl, Long itemNo);
}