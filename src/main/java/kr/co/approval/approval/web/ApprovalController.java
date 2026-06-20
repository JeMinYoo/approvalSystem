package kr.co.approval.approval.web;

import java.util.Arrays;
import java.util.List;

import kr.co.approval.approval.dto.ApprovalActionRequest;
import kr.co.approval.approval.dto.ApprovalCreateRequest;
import kr.co.approval.approval.dto.ApprovalDocument;
import kr.co.approval.approval.dto.ApprovalLine;
import kr.co.approval.approval.dto.ApprovalResult;
import kr.co.approval.approval.dto.ApprovalUpdateRequest;
import kr.co.approval.approval.service.ApprovalService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ApprovalController {

    private static final String LOGIN_USER_ID = "requester01";
    private static final String LOGIN_USER_NAME = "로그인 사용자";

    private static final List<ApprovalLine> APPROVAL_LINES = List.of(
            new ApprovalLine("LINE-TEAM-LEAD", "팀장 결재", "approver01", "팀장 1차 승인"),
            new ApprovalLine("LINE-MANAGER", "부서장 결재", "manager01", "부서장 승인"),
            new ApprovalLine("LINE-FINANCE", "재무 결재", "finance01", "재무 담당 승인")
    );

    private final ApprovalService approvalService;

    public ApprovalController(ApprovalService approvalService) {
        this.approvalService = approvalService;
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/approval";
    }

    @GetMapping("/approval")
    public String approvalHome(
            @RequestParam(name = "documentId", required = false) String documentId,
            @RequestParam(name = "mode", required = false) String mode,
            @RequestParam(name = "approverIds", required = false) String approverIds,
            Model model
    ) {
        if (documentId != null && !documentId.isBlank()) {
            ApprovalDocument selectedDocument = approvalService.findByDocumentId(documentId);
            model.addAttribute("selectedDocument", selectedDocument);
            model.addAttribute("editMode", "edit".equals(mode) && selectedDocument.isDraft());
            model.addAttribute("createRequest", createRequestFrom(selectedDocument));
        }
        if (!model.containsAttribute("createRequest")) {
            model.addAttribute("createRequest", defaultCreateRequest());
        }
        if (approverIds != null && !approverIds.isBlank()) {
            ApprovalCreateRequest createRequest = (ApprovalCreateRequest) model.getAttribute("createRequest");
            createRequest.setApproverIds(approverIds);
            model.addAttribute("createRequest", createRequest);
        }
        addHomeModel(model);
        return "approval/index";
    }

    @PostMapping("/approval/documents")
    public String createDocument(@ModelAttribute ApprovalCreateRequest createRequest, RedirectAttributes redirectAttributes) {
        createRequest.setRequesterId(LOGIN_USER_ID);
        ApprovalDocument document = approvalService.createDraft(createRequest);
        redirectAttributes.addFlashAttribute("message", "문서가 생성되었습니다. 문서번호: " + document.getDocumentId());
        return "redirect:/approval";
    }

    @PostMapping("/approval/documents/update")
    public String updateDocumentFromHome(@ModelAttribute ApprovalUpdateRequest updateRequest, RedirectAttributes redirectAttributes) {
        updateRequest.setRequesterId(LOGIN_USER_ID);
        updateRequest.setActorId(LOGIN_USER_ID);
        ApprovalResult result = approvalService.updateDraft(updateRequest);
        redirectAttributes.addFlashAttribute("message", result.getMessage());
        return "redirect:/approval?documentId=" + result.getDocument().getDocumentId();
    }

    @GetMapping("/approval/popup")
    public String approvalPopup(@RequestParam(name = "documentId", required = false) String documentId, Model model) {
        if (documentId == null || documentId.isBlank()) {
            addCreatePopupModel(model, defaultCreateRequest());
            return "approval/popup";
        }
        ApprovalDocument document = resolveDocument(documentId);
        addPopupModel(model, document, new ApprovalActionRequest());
        return "approval/popup";
    }

    @PostMapping("/approval/popup/documents")
    public String createDocumentFromPopup(@ModelAttribute ApprovalCreateRequest createRequest, Model model) {
        try {
            createRequest.setRequesterId(LOGIN_USER_ID);
            ApprovalDocument document = approvalService.createDraft(createRequest);
            addPopupModel(model, document, new ApprovalActionRequest());
            model.addAttribute("message", "문서가 생성되었습니다. 문서번호: " + document.getDocumentId());
            return "approval/popup";
        } catch (IllegalArgumentException | IllegalStateException exception) {
            addCreatePopupModel(model, createRequest);
            model.addAttribute("errorMessage", exception.getMessage());
            return "approval/popup";
        }
    }

    @GetMapping("/approval/line-popup")
    public String approvalLinePopup(
            @RequestParam(name = "selected", required = false) String selected,
            @RequestParam(name = "returnUrl", required = false) String returnUrl,
            Model model
    ) {
        addReferenceModel(model);
        model.addAttribute("selectedApproverIds", selected == null ? "" : selected);
        model.addAttribute("returnUrl", returnUrl == null ? "" : returnUrl);
        return "approval/line-popup";
    }

    @PostMapping("/approval/update")
    public String updateDocument(@ModelAttribute ApprovalUpdateRequest updateRequest, Model model) {
        updateRequest.setRequesterId(LOGIN_USER_ID);
        updateRequest.setActorId(LOGIN_USER_ID);
        runPopupAction(updateRequest.getDocumentId(), model, () -> approvalService.updateDraft(updateRequest));
        return "approval/popup";
    }

    @PostMapping("/approval/update-request")
    public String updateAndRequestApproval(@ModelAttribute ApprovalUpdateRequest updateRequest, Model model) {
        updateRequest.setRequesterId(LOGIN_USER_ID);
        updateRequest.setActorId(LOGIN_USER_ID);
        runPopupAction(updateRequest.getDocumentId(), model, () -> {
            approvalService.updateDraft(updateRequest);
            ApprovalActionRequest actionRequest = new ApprovalActionRequest();
            actionRequest.setDocumentId(updateRequest.getDocumentId());
            actionRequest.setActorId(LOGIN_USER_ID);
            actionRequest.setComment(updateRequest.getComment());
            return approvalService.requestApproval(actionRequest);
        });
        return "approval/popup";
    }

    @PostMapping("/approval/request")
    public String requestApproval(@ModelAttribute ApprovalActionRequest actionRequest, Model model) {
        runPopupAction(actionRequest.getDocumentId(), model, () -> approvalService.requestApproval(actionRequest));
        return "approval/popup";
    }

    @PostMapping("/approval/approve")
    public String approve(@ModelAttribute ApprovalActionRequest actionRequest, Model model) {
        runPopupAction(actionRequest.getDocumentId(), model, () -> approvalService.approve(actionRequest));
        return "approval/popup";
    }

    @PostMapping("/approval/reject")
    public String reject(@ModelAttribute ApprovalActionRequest actionRequest, Model model) {
        runPopupAction(actionRequest.getDocumentId(), model, () -> approvalService.reject(actionRequest));
        return "approval/popup";
    }

    @PostMapping("/approval/withdraw")
    public String withdraw(@ModelAttribute ApprovalActionRequest actionRequest, Model model) {
        runPopupAction(actionRequest.getDocumentId(), model, () -> approvalService.withdraw(actionRequest));
        return "approval/popup";
    }

    @PostMapping("/approval/delete")
    public String deleteDocument(@ModelAttribute ApprovalActionRequest actionRequest, Model model) {
        if (runPopupAction(actionRequest.getDocumentId(), model, () -> approvalService.deleteDraft(actionRequest))) {
            model.addAttribute("deleted", true);
        }
        return "approval/popup";
    }

    @ExceptionHandler({ IllegalArgumentException.class, IllegalStateException.class })
    public String handleApprovalException(RuntimeException exception, Model model) {
        model.addAttribute("errorMessage", exception.getMessage());
        model.addAttribute("createRequest", defaultCreateRequest());
        addHomeModel(model);
        return "approval/index";
    }

    private ApprovalDocument resolveDocument(String documentId) {
        if (documentId == null || documentId.isBlank()) {
            if (approvalService.findAll().isEmpty()) {
                throw new IllegalStateException("처리할 결재 문서가 없습니다.");
            }
            return approvalService.findAll().get(0);
        }
        return approvalService.findByDocumentId(documentId);
    }

    private void addPopupModel(Model model, ApprovalDocument document, ApprovalActionRequest actionRequest) {
        if (actionRequest.getDocumentId() == null) {
            actionRequest.setDocumentId(document.getDocumentId());
        }
        if (actionRequest.getActorId() == null) {
            actionRequest.setActorId(document.isEditable() ? document.getRequesterId() : document.getApproverId());
        }
        model.addAttribute("document", document);
        model.addAttribute("actionRequest", actionRequest);
        addReferenceModel(model);
    }

    private void addCreatePopupModel(Model model, ApprovalCreateRequest createRequest) {
        createRequest.setRequesterId(LOGIN_USER_ID);
        model.addAttribute("createMode", true);
        model.addAttribute("createRequest", createRequest);
        model.addAttribute("createApproverLineText", approvalLineText(createRequest.getApproverIds()));
        addReferenceModel(model);
    }

    private void addHomeModel(Model model) {
        model.addAttribute("documents", approvalService.findAll());
        Object createRequest = model.asMap().get("createRequest");
        if (createRequest instanceof ApprovalCreateRequest request) {
            model.addAttribute("createApproverLineText", approvalLineText(request.getApproverIds()));
        }
        addReferenceModel(model);
    }

    private void addReferenceModel(Model model) {
        model.addAttribute("loginUserId", LOGIN_USER_ID);
        model.addAttribute("loginUserName", LOGIN_USER_NAME);
        model.addAttribute("approvalLines", APPROVAL_LINES);
    }

    private boolean runPopupAction(String documentId, Model model, PopupAction popupAction) {
        try {
            ApprovalResult result = popupAction.run();
            addPopupModel(model, result.getDocument(), new ApprovalActionRequest());
            model.addAttribute("message", result.getMessage());
            return true;
        } catch (IllegalArgumentException | IllegalStateException exception) {
            ApprovalDocument document = approvalService.findByDocumentId(documentId);
            addPopupModel(model, document, new ApprovalActionRequest());
            model.addAttribute("errorMessage", exception.getMessage());
            return false;
        }
    }

    private ApprovalCreateRequest defaultCreateRequest() {
        ApprovalCreateRequest request = new ApprovalCreateRequest();
        request.setTitle("연계모듈 결재 요청");
        request.setRequesterId(LOGIN_USER_ID);
        request.setApproverIds(APPROVAL_LINES.get(0).getApproverId());
        request.setContent("");
        return request;
    }

    private ApprovalCreateRequest createRequestFrom(ApprovalDocument document) {
        ApprovalCreateRequest request = new ApprovalCreateRequest();
        request.setTitle(document.getTitle());
        request.setRequesterId(document.getRequesterId());
        request.setApproverIds(document.getApproverIdsText());
        request.setContent(document.getContent());
        return request;
    }

    private String approvalLineText(String approverIds) {
        if (approverIds == null || approverIds.isBlank()) {
            return "";
        }
        return String.join(" > ", Arrays.stream(approverIds.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .map(this::approvalLineLabel)
                .toList());
    }

    private String approvalLineLabel(String approverId) {
        return APPROVAL_LINES.stream()
                .filter(line -> line.getApproverId().equals(approverId))
                .findFirst()
                .map(line -> line.getLineName() + " - " + line.getDescription() + " (" + line.getApproverId() + ")")
                .orElse(approverId);
    }

    @FunctionalInterface
    private interface PopupAction {
        ApprovalResult run();
    }
}
