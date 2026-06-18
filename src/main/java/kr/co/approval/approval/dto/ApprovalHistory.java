package kr.co.approval.approval.dto;

import java.time.LocalDateTime;

public class ApprovalHistory {

    private final ApprovalStatus status;
    private final String actorId;
    private final String comment;
    private final LocalDateTime processedAt;

    public ApprovalHistory(ApprovalStatus status, String actorId, String comment, LocalDateTime processedAt) {
        this.status = status;
        this.actorId = actorId;
        this.comment = comment;
        this.processedAt = processedAt;
    }

    public ApprovalStatus getStatus() {
        return status;
    }

    public String getActorId() {
        return actorId;
    }

    public String getComment() {
        return comment;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }
}
