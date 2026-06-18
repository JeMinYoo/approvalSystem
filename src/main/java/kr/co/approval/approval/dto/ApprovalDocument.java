package kr.co.approval.approval.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

public class ApprovalDocument {

    private final String documentId;
    private String title;
    private String requesterId;
    private List<String> approverIds;
    private int currentApproverIndex;
    private String content;
    private ApprovalStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime requestedAt;
    private LocalDateTime decidedAt;
    private final List<ApprovalHistory> histories = new ArrayList<>();

    public ApprovalDocument(String documentId, String title, String requesterId, List<String> approverIds, String content) {
        this.documentId = documentId;
        this.title = title;
        this.requesterId = requesterId;
        this.approverIds = new ArrayList<>(approverIds);
        this.currentApproverIndex = 0;
        this.content = content;
        this.status = ApprovalStatus.DRAFT;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public String getDocumentId() {
        return documentId;
    }

    public String getTitle() {
        return title;
    }

    public String getRequesterId() {
        return requesterId;
    }

    public String getApproverId() {
        if (approverIds.isEmpty()) {
            return "";
        }
        return approverIds.get(Math.min(currentApproverIndex, approverIds.size() - 1));
    }

    public List<String> getApproverIds() {
        return Collections.unmodifiableList(approverIds);
    }

    public String getApproverIdsText() {
        StringJoiner joiner = new StringJoiner(",");
        for (String approverId : approverIds) {
            joiner.add(approverId);
        }
        return joiner.toString();
    }

    public String getApproverLineText() {
        return String.join(" > ", approverIds);
    }

    public int getApprovalStep() {
        return currentApproverIndex + 1;
    }

    public int getApprovalStepCount() {
        return approverIds.size();
    }

    public boolean isLastApprover() {
        return currentApproverIndex >= approverIds.size() - 1;
    }

    public void moveToNextApprover() {
        if (!isLastApprover()) {
            currentApproverIndex++;
        }
    }

    public void resetApprovalProgress() {
        currentApproverIndex = 0;
    }

    public String getContent() {
        return content;
    }

    public void update(String title, String requesterId, List<String> approverIds, String content, LocalDateTime updatedAt) {
        this.title = title;
        this.requesterId = requesterId;
        this.approverIds = new ArrayList<>(approverIds);
        this.currentApproverIndex = 0;
        this.content = content;
        this.updatedAt = updatedAt;
    }

    public ApprovalStatus getStatus() {
        return status;
    }

    public void setStatus(ApprovalStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(LocalDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }

    public LocalDateTime getDecidedAt() {
        return decidedAt;
    }

    public void setDecidedAt(LocalDateTime decidedAt) {
        this.decidedAt = decidedAt;
    }

    public List<ApprovalHistory> getHistories() {
        return Collections.unmodifiableList(histories);
    }

    public void addHistory(ApprovalHistory history) {
        histories.add(history);
    }

    public boolean isDraft() {
        return status == ApprovalStatus.DRAFT;
    }

    public boolean isEditable() {
        return status == ApprovalStatus.DRAFT;
    }

    public boolean isRequested() {
        return status == ApprovalStatus.REQUESTED;
    }

    public boolean isFinished() {
        return status == ApprovalStatus.APPROVED
                || status == ApprovalStatus.REJECTED;
    }

    public boolean isDeletable() {
        return isEditable();
    }

    public boolean isWithdrawable() {
        return status == ApprovalStatus.REQUESTED;
    }

    public boolean isDecidable() {
        return status == ApprovalStatus.REQUESTED;
    }
}
