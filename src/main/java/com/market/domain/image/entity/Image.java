package com.market.domain.image.entity;

import com.market.domain.inquiry.entity.Inquiry;
import com.market.domain.inquiryAnswer.entity.InquiryAnswer;
import com.market.domain.item.entity.Item;
import com.market.domain.market.entity.Market;
import com.market.domain.notice.entity.Notice;
import com.market.domain.shop.entity.Shop;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "image")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_no")
    private Long no;

    @Column(nullable = false)
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "market_no")
    private Market market;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_no")
    private Shop shop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_no")
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inquiry_no")
    private Inquiry inquiry;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notice_no")
    private Notice notice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "answer_no")
    private InquiryAnswer inquiryAnswer;

    public Image(Market market, String urlText) {
        this.market = market;
        this.imageUrl = urlText;
    }

    public Image(Shop shop, String urlText) {
        this.shop = shop;
        this.imageUrl = urlText;
    }

    public Image(Item item, String urlText) {
        this.item = item;
        this.imageUrl = urlText;
    }

    public Image(Inquiry inquiry, String urlText) {
        this.inquiry = inquiry;
        this.imageUrl = urlText;
    }

    public Image(Notice notice, String urlText) {
        this.notice = notice;
        this.imageUrl = urlText;
    }

    public Image(InquiryAnswer inquiryAnswer, String urlText) {
        this.inquiryAnswer = inquiryAnswer;
        this.imageUrl = urlText;
    }
}

