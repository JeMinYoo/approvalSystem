package kr.co.approval.approval.dto;

public enum ApprovalStatus {
    DRAFT("작성중"),
    REQUESTED("결재요청"),
    APPROVED("승인"),
    REJECTED("반려"),
    WITHDRAWN("회수");

    private final String displayName;

    ApprovalStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getCssName() {
        return name().toLowerCase();
    }
}
