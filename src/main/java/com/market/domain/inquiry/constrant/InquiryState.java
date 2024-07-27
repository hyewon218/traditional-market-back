package com.market.domain.inquiry.constrant;

public enum InquiryState {
    ANSWER_COMPLETED("답변 완료"),
    ANSWER_PENDING("답변 미완료");

    private final String displayName;

    InquiryState(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
