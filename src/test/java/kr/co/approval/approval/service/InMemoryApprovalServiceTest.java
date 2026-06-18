package kr.co.approval.approval.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import kr.co.approval.approval.dto.ApprovalActionRequest;
import kr.co.approval.approval.dto.ApprovalCreateRequest;
import kr.co.approval.approval.dto.ApprovalDocument;
import kr.co.approval.approval.dto.ApprovalStatus;
import kr.co.approval.approval.dto.ApprovalUpdateRequest;
import org.junit.jupiter.api.Test;

class InMemoryApprovalServiceTest {

    private final InMemoryApprovalService approvalService = new InMemoryApprovalService();

    @Test
    void approvalCanMoveFromDraftToRequestedToApproved() {
        ApprovalDocument document = approvalService.createDraft(createRequest());

        approvalService.requestApproval(actionRequest(document.getDocumentId(), "requester01"));
        assertEquals(ApprovalStatus.REQUESTED, document.getStatus());

        approvalService.approve(actionRequest(document.getDocumentId(), "approver01"));
        assertEquals(ApprovalStatus.REQUESTED, document.getStatus());
        assertEquals("manager01", document.getApproverId());

        approvalService.approve(actionRequest(document.getDocumentId(), "manager01"));
        assertEquals(ApprovalStatus.APPROVED, document.getStatus());
        assertEquals(4, document.getHistories().size());
    }

    @Test
    void draftDocumentCannotBeApprovedBeforeRequest() {
        ApprovalDocument document = approvalService.createDraft(createRequest());

        ApprovalActionRequest actionRequest = actionRequest(document.getDocumentId(), "approver01");
        assertThrows(IllegalStateException.class, () -> approvalService.approve(actionRequest));
    }

    @Test
    void requesterCanUpdateEditableDocument() {
        ApprovalDocument document = approvalService.createDraft(createRequest());

        ApprovalUpdateRequest updateRequest = updateRequest(document.getDocumentId());
        approvalService.updateDraft(updateRequest);

        assertEquals("수정된 테스트 결재", document.getTitle());
        assertEquals(2, document.getHistories().size());
    }

    @Test
    void nonRequesterCannotRequestApproval() {
        ApprovalDocument document = approvalService.createDraft(createRequest());

        ApprovalActionRequest actionRequest = actionRequest(document.getDocumentId(), "other01");
        assertThrows(IllegalStateException.class, () -> approvalService.requestApproval(actionRequest));
    }

    @Test
    void nonApproverCannotApprove() {
        ApprovalDocument document = approvalService.createDraft(createRequest());
        approvalService.requestApproval(actionRequest(document.getDocumentId(), "requester01"));

        ApprovalActionRequest actionRequest = actionRequest(document.getDocumentId(), "other01");
        assertThrows(IllegalStateException.class, () -> approvalService.approve(actionRequest));
    }

    @Test
    void requesterCanWithdrawRequestedDocumentToDraft() {
        ApprovalDocument document = approvalService.createDraft(createRequest());
        approvalService.requestApproval(actionRequest(document.getDocumentId(), "requester01"));

        approvalService.withdraw(actionRequest(document.getDocumentId(), "requester01"));

        assertEquals(ApprovalStatus.DRAFT, document.getStatus());
    }

    @Test
    void rejectRequiresComment() {
        ApprovalDocument document = approvalService.createDraft(createRequest());
        approvalService.requestApproval(actionRequest(document.getDocumentId(), "requester01"));

        ApprovalActionRequest actionRequest = actionRequest(document.getDocumentId(), "approver01");
        actionRequest.setComment("");
        assertThrows(IllegalArgumentException.class, () -> approvalService.reject(actionRequest));
    }

    @Test
    void requesterCanDeleteEditableDocument() {
        ApprovalDocument document = approvalService.createDraft(createRequest());

        approvalService.deleteDraft(actionRequest(document.getDocumentId(), "requester01"));

        assertThrows(IllegalArgumentException.class, () -> approvalService.findByDocumentId(document.getDocumentId()));
    }

    private ApprovalCreateRequest createRequest() {
        ApprovalCreateRequest request = new ApprovalCreateRequest();
        request.setTitle("테스트 결재");
        request.setRequesterId("requester01");
        request.setApproverIds("approver01,manager01");
        request.setContent("테스트 결재 내용");
        return request;
    }

    private ApprovalActionRequest actionRequest(String documentId, String actorId) {
        ApprovalActionRequest request = new ApprovalActionRequest();
        request.setDocumentId(documentId);
        request.setActorId(actorId);
        request.setComment("처리 의견");
        return request;
    }

    private ApprovalUpdateRequest updateRequest(String documentId) {
        ApprovalUpdateRequest request = new ApprovalUpdateRequest();
        request.setDocumentId(documentId);
        request.setTitle("수정된 테스트 결재");
        request.setRequesterId("requester01");
        request.setApproverIds("approver01,manager01");
        request.setContent("수정된 테스트 결재 내용");
        request.setActorId("requester01");
        request.setComment("수정 테스트");
        return request;
    }
}
