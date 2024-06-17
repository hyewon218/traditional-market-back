package com.market.domain.image.dto;

import com.market.domain.image.entity.Image;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ImageResponseDto {

    private String imageUrl;

    public static ImageResponseDto of(Image image) {
        return ImageResponseDto.builder()
            .imageUrl(image.getImageUrl())
            .build();
    }
}