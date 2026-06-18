package kr.co.approval.approval.dto;

public class ApprovalUpdateRequest {

    private String documentId;
    private String title;
    private String requesterId;
    private String approverId;
    private String approverIds;
    private String content;
    private String actorId;
    private String comment;

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(String requesterId) {
        this.requesterId = requesterId;
    }

    public String getApproverId() {
        if (approverId != null && !approverId.isBlank()) {
            return approverId;
        }
        if (approverIds == null || approverIds.isBlank()) {
            return approverId;
        }
        return approverIds.split(",")[0].trim();
    }

    public void setApproverId(String approverId) {
        this.approverId = approverId;
        this.approverIds = approverId;
    }

    public String getApproverIds() {
        return approverIds;
    }

    public void setApproverIds(String approverIds) {
        this.approverIds = approverIds;
        this.approverId = getApproverId();
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getActorId() {
        return actorId;
    }

    public void setActorId(String actorId) {
        this.actorId = actorId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
