package com.market.domain.delivery.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class DeliveryUpdateRequestDto {

    @NotBlank(message = "배송지 이름을 입력해주세요")
    private String title;

    @NotBlank(message = "받는 사람 이름을 입력해주세요")
    private String receiver;

    @NotBlank(message = "휴대전화번호를 입력해주세요")
    private String phone;

    @NotBlank(message = "주소검색을 통해 주소를 입력해주세요")
    private String postCode;
    private String roadAddr;
    private String jibunAddr;

    @NotBlank(message = "상세주소를 입력해주세요")
    private String detailAddr;

    private String extraAddr;

    private boolean isPrimary;
}
