package kr.co.approval.approval.dto;

public class ApprovalResult {

    private final ApprovalDocument document;
    private final String message;

    public ApprovalResult(ApprovalDocument document, String message) {
        this.document = document;
        this.message = message;
    }

    public ApprovalDocument getDocument() {
        return document;
    }

    public String getMessage() {
        return message;
    }
}
