package kr.co.approval.approval.service;

import java.util.List;

import kr.co.approval.approval.dto.ApprovalActionRequest;
import kr.co.approval.approval.dto.ApprovalCreateRequest;
import kr.co.approval.approval.dto.ApprovalDocument;
import kr.co.approval.approval.dto.ApprovalResult;
import kr.co.approval.approval.dto.ApprovalUpdateRequest;

public interface ApprovalService {

    List<ApprovalDocument> findAll();

    ApprovalDocument findByDocumentId(String documentId);

    ApprovalDocument createDraft(ApprovalCreateRequest request);

    ApprovalResult updateDraft(ApprovalUpdateRequest request);

    ApprovalResult requestApproval(ApprovalActionRequest request);

    ApprovalResult approve(ApprovalActionRequest request);

    ApprovalResult reject(ApprovalActionRequest request);

    ApprovalResult withdraw(ApprovalActionRequest request);

    ApprovalResult deleteDraft(ApprovalActionRequest request);
}
