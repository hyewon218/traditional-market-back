package com.market.domain.image.repository;

import com.market.domain.image.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {

    Boolean existsByImageUrlAndNo(String fileName, Long no);
}