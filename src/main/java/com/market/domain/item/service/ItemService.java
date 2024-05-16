package com.market.domain.item.service;

import com.market.domain.item.dto.ItemRequestDto;
import com.market.domain.item.dto.ItemResponseDto;
import com.market.domain.item.entity.Item;
import java.io.IOException;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

public interface ItemService {

    /**
     * 상점 생성
     *
     * @param requestDto : 상점 생성 요청정보
     * @param files      : 상점 생성 첨부파일
     */
    void createItem(ItemRequestDto requestDto, List<MultipartFile> files) throws IOException;

    /**
     * 상점 목록 조회
     *
     * @return : 조회된 상점들 정보
     */
    Page<ItemResponseDto> getItems(int page, int size, String sortBy, boolean isAsc);

    /**
     * 상점 단건 조회
     *
     * @param itemNo : 조회 할 상점 no
     * @return : 조회된 상점 단건 정보
     */
    ItemResponseDto getItem(Long itemNo);

    /**
     * 상점 수정
     *
     * @param itemNo   : 수정할 상점 no
     * @param requestDto : 상점 수정 요청정보
     * @param files      : 상점 수정 첨부파일
     */
    void updateItem(Long itemNo, ItemRequestDto requestDto, List<MultipartFile> files)
        throws IOException;

    /**
     * 상점 삭제
     *
     * @param itemNo : 삭제할 상점 no
     */
    void deleteItem(Long itemNo);

    /**
     * 상점 찾기
     *
     * @param itemNo : 찾을 상점 no
     * @return : 상점 Entity
     */
    Item findItem(Long itemNo);
}
