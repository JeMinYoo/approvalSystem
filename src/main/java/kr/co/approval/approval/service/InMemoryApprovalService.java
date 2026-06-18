package kr.co.approval.approval.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import kr.co.approval.approval.dto.ApprovalActionRequest;
import kr.co.approval.approval.dto.ApprovalCreateRequest;
import kr.co.approval.approval.dto.ApprovalDocument;
import kr.co.approval.approval.dto.ApprovalHistory;
import kr.co.approval.approval.dto.ApprovalResult;
import kr.co.approval.approval.dto.ApprovalStatus;
import kr.co.approval.approval.dto.ApprovalUpdateRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class InMemoryApprovalService implements ApprovalService {

    private static final DateTimeFormatter DOCUMENT_DATE_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;

    private final AtomicInteger sequence = new AtomicInteger(1);
    private final Map<String, ApprovalDocument> documents = new ConcurrentHashMap<>();

    public InMemoryApprovalService() {
        ApprovalCreateRequest sample = new ApprovalCreateRequest();
        sample.setTitle("연계모듈 결재 요청 샘플");
        sample.setRequesterId("requester01");
        sample.setApproverIds("approver01,manager01");
        sample.setContent("외부 시스템 연계 처리를 위한 결재 샘플 문서입니다.");
        createDraft(sample);
    }

    @Override
    public List<ApprovalDocument> findAll() {
        return documents.values().stream()
                .sorted(Comparator.comparing(ApprovalDocument::getCreatedAt).reversed())
                .toList();
    }

    @Override
    public ApprovalDocument findByDocumentId(String documentId) {
        ApprovalDocument document = documents.get(documentId);
        if (document == null) {
            throw new IllegalArgumentException("결재 문서를 찾을 수 없습니다. documentId=" + documentId);
        }
        return document;
    }

    @Override
    public ApprovalDocument createDraft(ApprovalCreateRequest request) {
        validateRequired(request.getTitle(), "제목");
        validateRequired(request.getRequesterId(), "요청자");
        List<String> approverIds = parseApproverIds(request.getApproverIds());
        validateDifferentUsers(request.getRequesterId(), approverIds);

        String documentId = nextDocumentId();
        ApprovalDocument document = new ApprovalDocument(
                documentId,
                request.getTitle(),
                request.getRequesterId(),
                approverIds,
                defaultText(request.getContent())
        );
        document.addHistory(new ApprovalHistory(
                ApprovalStatus.DRAFT,
                request.getRequesterId(),
                "문서가 작성되었습니다.",
                document.getCreatedAt()
        ));
        documents.put(documentId, document);
        return document;
    }

    @Override
    public ApprovalResult updateDraft(ApprovalUpdateRequest request) {
        ApprovalDocument document = findByDocumentId(request.getDocumentId());
        ensureEditable(document);
        ensureRequester(document, request.getActorId());
        validateRequired(request.getTitle(), "제목");
        validateRequired(request.getRequesterId(), "요청자");
        List<String> approverIds = parseApproverIds(request.getApproverIds());
        validateDifferentUsers(request.getRequesterId(), approverIds);

        LocalDateTime now = LocalDateTime.now();
        document.update(
                request.getTitle(),
                request.getRequesterId(),
                approverIds,
                defaultText(request.getContent()),
                now
        );
        document.addHistory(new ApprovalHistory(
                ApprovalStatus.DRAFT,
                defaultActor(request.getActorId(), document.getRequesterId()),
                defaultText(request.getComment(), "문서가 수정되었습니다."),
                now
        ));
        return new ApprovalResult(document, "문서가 수정되었습니다.");
    }

    @Override
    public ApprovalResult requestApproval(ApprovalActionRequest request) {
        ApprovalDocument document = findByDocumentId(request.getDocumentId());
        ensureEditable(document);
        ensureRequester(document, request.getActorId());

        LocalDateTime now = LocalDateTime.now();
        document.setStatus(ApprovalStatus.REQUESTED);
        document.resetApprovalProgress();
        document.setRequestedAt(now);
        document.setDecidedAt(null);
        document.addHistory(new ApprovalHistory(
                ApprovalStatus.REQUESTED,
                defaultActor(request.getActorId(), document.getRequesterId()),
                defaultText(request.getComment(), "결재를 요청했습니다."),
                now
        ));
        return new ApprovalResult(document, "결재 요청이 완료되었습니다.");
    }

    @Override
    public ApprovalResult approve(ApprovalActionRequest request) {
        ApprovalDocument document = findRequestedDocument(request.getDocumentId());
        ensureApprover(document, request.getActorId());
        LocalDateTime now = LocalDateTime.now();
        String message;
        if (document.isLastApprover()) {
            document.setStatus(ApprovalStatus.APPROVED);
            document.setDecidedAt(now);
            message = "결재가 최종 승인되었습니다.";
        } else {
            document.moveToNextApprover();
            message = "승인되었습니다. 다음 결재자: " + document.getApproverId();
        }
        document.addHistory(new ApprovalHistory(
                ApprovalStatus.APPROVED,
                request.getActorId(),
                defaultText(request.getComment(), "승인되었습니다."),
                now
        ));
        return new ApprovalResult(document, message);
    }

    @Override
    public ApprovalResult reject(ApprovalActionRequest request) {
        ApprovalDocument document = findRequestedDocument(request.getDocumentId());
        ensureApprover(document, request.getActorId());
        validateRequired(request.getComment(), "반려 의견");
        LocalDateTime now = LocalDateTime.now();
        document.setStatus(ApprovalStatus.REJECTED);
        document.setDecidedAt(now);
        document.addHistory(new ApprovalHistory(
                ApprovalStatus.REJECTED,
                defaultActor(request.getActorId(), document.getApproverId()),
                defaultText(request.getComment(), "반려되었습니다."),
                now
        ));
        return new ApprovalResult(document, "결재가 반려되었습니다.");
    }

    @Override
    public ApprovalResult withdraw(ApprovalActionRequest request) {
        ApprovalDocument document = findRequestedDocument(request.getDocumentId());
        ensureRequester(document, request.getActorId());

        LocalDateTime now = LocalDateTime.now();
        document.setStatus(ApprovalStatus.DRAFT);
        document.setRequestedAt(null);
        document.setDecidedAt(null);
        document.addHistory(new ApprovalHistory(
                ApprovalStatus.DRAFT,
                defaultActor(request.getActorId(), document.getRequesterId()),
                defaultText(request.getComment(), "결재 요청을 회수하여 작성중으로 변경했습니다."),
                now
        ));
        return new ApprovalResult(document, "결재 요청이 회수되어 작성중으로 변경되었습니다.");
    }

    @Override
    public ApprovalResult deleteDraft(ApprovalActionRequest request) {
        ApprovalDocument document = findByDocumentId(request.getDocumentId());
        ensureEditable(document);
        ensureRequester(document, request.getActorId());
        documents.remove(document.getDocumentId());
        return new ApprovalResult(document, "문서가 삭제되었습니다.");
    }

    private ApprovalDocument findRequestedDocument(String documentId) {
        ApprovalDocument document = findByDocumentId(documentId);
        if (document.getStatus() != ApprovalStatus.REQUESTED) {
            throw new IllegalStateException("결재요청 상태의 문서만 처리할 수 있습니다.");
        }
        return document;
    }

    private void ensureEditable(ApprovalDocument document) {
        if (!document.isEditable()) {
            throw new IllegalStateException("작성중 상태의 문서만 수정, 삭제, 상신할 수 있습니다.");
        }
    }

    private void ensureRequester(ApprovalDocument document, String actorId) {
        validateRequired(actorId, "처리자");
        if (!document.getRequesterId().equals(actorId)) {
            throw new IllegalStateException("요청자만 처리할 수 있습니다. 요청자=" + document.getRequesterId());
        }
    }

    private void ensureApprover(ApprovalDocument document, String actorId) {
        validateRequired(actorId, "처리자");
        if (!document.getApproverId().equals(actorId)) {
            throw new IllegalStateException("현재 결재자만 처리할 수 있습니다. 현재 결재자=" + document.getApproverId());
        }
    }

    private List<String> parseApproverIds(String approverIdsText) {
        validateRequired(approverIdsText, "결재라인");
        List<String> approverIds = Arrays.stream(approverIdsText.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
        if (approverIds.isEmpty()) {
            throw new IllegalArgumentException("결재라인을 1명 이상 선택해야 합니다.");
        }
        return approverIds;
    }

    private void validateDifferentUsers(String requesterId, List<String> approverIds) {
        if (approverIds.contains(requesterId)) {
            throw new IllegalArgumentException("요청자는 결재라인에 포함될 수 없습니다.");
        }
    }

    private String nextDocumentId() {
        return "DOC-" + LocalDate.now().format(DOCUMENT_DATE_FORMAT) + "-" + String.format("%03d", sequence.getAndIncrement());
    }

    private void validateRequired(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(fieldName + "은(는) 필수입니다.");
        }
    }

    private String defaultActor(String requestedActor, String fallbackActor) {
        return StringUtils.hasText(requestedActor) ? requestedActor : fallbackActor;
    }

    private String defaultText(String text) {
        return defaultText(text, "");
    }

    private String defaultText(String text, String fallback) {
        return StringUtils.hasText(text) ? text : fallback;
    }
}
