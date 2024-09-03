package com.market.domain.image.dto;

import com.market.domain.image.entity.Image;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ImageResponseDto {

    private String imageUrl;

    public static ImageResponseDto of(Image image) {
        return ImageResponseDto.builder()
            .imageUrl(image.getImageUrl())
            .build();
    }
}