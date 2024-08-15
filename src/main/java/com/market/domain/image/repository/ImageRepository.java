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

    List<Image> findByInquiry_InquiryNo(Long inquiryNo);

    List<Image> findByInquiryAnswer_AnswerNo(Long answerNo);

    List<Image> findByNotice_NoticeNo(Long noticeNo);

    Boolean existsByImageUrlAndMarket_No(String imageUrl, Long marketNo);

    Boolean existsByImageUrlAndShop_No(String imageUrl, Long shopNo);

    Boolean existsByImageUrlAndItem_No(String imageUrl, Long no);

    Boolean existsByImageUrlAndInquiry_InquiryNo(String imageUrl, Long inquiryNo);

    Boolean existsByImageUrlAndInquiryAnswer_AnswerNo(String imageUrl, Long answerNo);

    Boolean existsByImageUrlAndNotice_NoticeNo(String imageUrl, Long noticeNo);

    void deleteByImageUrlAndMarket_No(String defaultImageUrl, Long marketNo);

    void deleteByImageUrlAndShop_No(String defaultImageUrl, Long shopNo);

    void deleteByImageUrlAndItem_No(String defaultImageUrl, Long itemNo);
}