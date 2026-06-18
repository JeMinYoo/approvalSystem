<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!doctype html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>결재 시스템</title>
    <link rel="stylesheet" href="<c:url value='/resources/css/approval.css' />">
</head>
<body>
<main class="shell">
    <header class="topbar">
        <div>
            <p class="eyebrow">Approval Module</p>
            <h1>결재 시스템</h1>
        </div>
        <c:if test="${fn:length(documents) > 0}">
            <button class="primary-button" type="button" onclick="openApprovalPopup()">
                팝업 열기
            </button>
        </c:if>
    </header>

    <c:if test="${not empty message}">
        <section class="notice">
            <strong>처리 완료</strong>
            <span>${message}</span>
        </section>
    </c:if>

    <c:if test="${not empty errorMessage}">
        <section class="notice error">
            <strong>처리 실패</strong>
            <span>${errorMessage}</span>
        </section>
    </c:if>

    <section class="workspace">
        <article class="form-panel">
            <div class="section-title">
                <h2>
                    <c:choose>
                        <c:when test="${empty selectedDocument}">신규 결재 문서</c:when>
                        <c:when test="${editMode}">결재 문서 수정</c:when>
                        <c:otherwise>결재 문서 내용</c:otherwise>
                    </c:choose>
                </h2>
                <c:if test="${not empty selectedDocument}">
                    <span class="status status-${selectedDocument.status.cssName}">
                        ${selectedDocument.status.displayName}
                    </span>
                </c:if>
            </div>

            <c:choose>
                <c:when test="${empty selectedDocument}">
                    <form method="post" action="<c:url value='/approval/documents' />" class="approval-form">
                        <label>
                            제목
                            <input name="title" value="${createRequest.title}" required>
                        </label>
                        <div class="field-grid">
                            <label>
                                요청자
                                <input value="${loginUserName} (${loginUserId})" readonly>
                                <input type="hidden" name="requesterId" value="${loginUserId}">
                            </label>
                            <label>
                                결재라인
                                <input id="createApproverIds" type="hidden" name="approverIds" value="${createRequest.approverIds}">
                                <div class="line-picker">
                                    <input id="createApproverDisplay" value="${createApproverLineText}" readonly>
                                    <button type="button" class="secondary-button" onclick="openApprovalLinePopup('createApproverIds', 'createApproverDisplay')">
                                        결재라인 선택
                                    </button>
                                </div>
                            </label>
                        </div>
                        <label>
                            내용
                            <textarea name="content" rows="5" placeholder="연계 처리 대상, 요청 사유, 참고 사항을 입력하세요." required>${createRequest.content}</textarea>
                        </label>
                        <div class="actions">
                            <button type="submit" class="primary-button">문서 생성</button>
                        </div>
                    </form>
                </c:when>

                <c:when test="${editMode}">
                    <dl class="mini-summary">
                        <div>
                            <dt>문서번호</dt>
                            <dd>${selectedDocument.documentId}</dd>
                        </div>
                        <div>
                            <dt>현재 상태</dt>
                            <dd>${selectedDocument.status.displayName}</dd>
                        </div>
                    </dl>

                    <form method="post" action="<c:url value='/approval/documents/update' />" class="approval-form">
                        <input type="hidden" name="documentId" value="${selectedDocument.documentId}">
                        <input type="hidden" name="comment" value="메인 화면에서 문서를 수정했습니다.">
                        <label>
                            제목
                            <input name="title" value="${createRequest.title}" required>
                        </label>
                        <div class="field-grid">
                            <label>
                                요청자
                                <input value="${loginUserName} (${loginUserId})" readonly>
                                <input type="hidden" name="requesterId" value="${loginUserId}">
                            </label>
                            <label>
                                결재라인
                                <input id="editApproverIds" type="hidden" name="approverIds" value="${createRequest.approverIds}">
                                <div class="line-picker">
                                    <input id="editApproverDisplay" value="${createApproverLineText}" readonly>
                                    <button type="button" class="secondary-button" onclick="openApprovalLinePopup('editApproverIds', 'editApproverDisplay')">
                                        결재라인 선택
                                    </button>
                                </div>
                            </label>
                        </div>
                        <label>
                            내용
                            <textarea name="content" rows="7" placeholder="연계 처리 대상, 요청 사유, 참고 사항을 입력하세요." required>${createRequest.content}</textarea>
                        </label>
                        <div class="actions">
                            <button type="button" class="secondary-button" onclick="location.href='<c:url value="/approval" />?documentId=${selectedDocument.documentId}'">
                                취소
                            </button>
                            <button type="submit" class="primary-button">수정 저장</button>
                        </div>
                    </form>
                </c:when>

                <c:otherwise>
                    <dl class="summary detail-summary">
                        <div>
                            <dt>문서번호</dt>
                            <dd>${selectedDocument.documentId}</dd>
                        </div>
                        <div>
                            <dt>제목</dt>
                            <dd>${selectedDocument.title}</dd>
                        </div>
                        <div>
                            <dt>요청자</dt>
                            <dd>${selectedDocument.requesterId}</dd>
                        </div>
                        <div>
                            <dt>결재라인</dt>
                            <dd>${selectedDocument.approverLineText}</dd>
                        </div>
                        <div>
                            <dt>현재 결재자</dt>
                            <dd>${selectedDocument.approverId} (${selectedDocument.approvalStep}/${selectedDocument.approvalStepCount})</dd>
                        </div>
                    </dl>

                    <article class="content-box selected-content">
                        <h2>내용</h2>
                        <p>${selectedDocument.content}</p>
                    </article>

                    <div class="actions">
                        <button type="button" class="secondary-button" onclick="location.href='<c:url value="/approval" />'">
                            새 문서
                        </button>
                        <button type="button" class="secondary-button" onclick="openApprovalPopup('${selectedDocument.documentId}')">
                            팝업 처리
                        </button>
                        <c:if test="${selectedDocument.draft}">
                            <button type="button" class="primary-button" onclick="location.href='<c:url value="/approval" />?documentId=${selectedDocument.documentId}&mode=edit'">
                                수정
                            </button>
                        </c:if>
                    </div>
                </c:otherwise>
            </c:choose>
        </article>

        <article class="list-panel">
            <div class="section-title">
                <h2>결재 문서 목록</h2>
                <span>${fn:length(documents)}건</span>
            </div>

            <div class="table-wrap">
                <table>
                    <thead>
                    <tr>
                        <th>문서번호</th>
                        <th>제목</th>
                        <th>요청자</th>
                        <th>결재라인</th>
                        <th>상태</th>
                        <th>요청일시</th>
                        <th></th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:choose>
                        <c:when test="${fn:length(documents) == 0}">
                            <tr>
                                <td colspan="7" class="empty-cell">등록된 결재 문서가 없습니다.</td>
                            </tr>
                        </c:when>
                        <c:otherwise>
                            <c:forEach var="document" items="${documents}">
                                <c:url var="documentViewUrl" value="/approval">
                                    <c:param name="documentId" value="${document.documentId}" />
                                </c:url>
                                <tr class="<c:if test='${not empty selectedDocument and selectedDocument.documentId eq document.documentId}'>selected-row</c:if>">
                                    <td>${document.documentId}</td>
                                    <td>
                                        <a class="title-link" href="${documentViewUrl}">
                                            ${document.title}
                                        </a>
                                    </td>
                                    <td>${document.requesterId}</td>
                                    <td>${document.approverLineText}</td>
                                    <td>
                                        <span class="status status-${document.status.cssName}">
                                            ${document.status.displayName}
                                        </span>
                                    </td>
                                    <td>${empty document.requestedAt ? '-' : document.requestedAt}</td>
                                    <td class="align-right">
                                        <button class="small-button" type="button" onclick="openApprovalPopup('${document.documentId}')">
                                            상세/처리
                                        </button>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>
                    </tbody>
                </table>
            </div>
        </article>
    </section>
</main>

<script>
    function openApprovalPopup(documentId) {
        const baseUrl = '<c:url value="/approval/popup" />';
        const url = documentId ? baseUrl + '?documentId=' + encodeURIComponent(documentId) : baseUrl;
        window.open(url, 'approvalPopup', 'width=760,height=760,menubar=no,toolbar=no,location=no,status=no');
    }

    function openApprovalLinePopup(hiddenId, displayId) {
        window.approvalLineTarget = { hiddenId: hiddenId, displayId: displayId };
        const selected = document.getElementById(hiddenId).value;
        const url = '<c:url value="/approval/line-popup" />'
            + '?selected=' + encodeURIComponent(selected)
            + '&returnUrl=' + encodeURIComponent(window.location.href);
        window.open(url, 'approvalLinePopup', 'width=560,height=620,menubar=no,toolbar=no,location=no,status=no');
    }

    function applyApprovalLineSelection(ids, labels) {
        if (!window.approvalLineTarget) {
            return;
        }
        document.getElementById(window.approvalLineTarget.hiddenId).value = ids;
        document.getElementById(window.approvalLineTarget.displayId).value = labels;
    }

    window.addEventListener('storage', function (event) {
        if (event.key !== 'approvalLineSelection' || !event.newValue) {
            return;
        }
        const selection = JSON.parse(event.newValue);
        applyApprovalLineSelection(selection.ids, selection.labels);
    });
</script>
</body>
</html>
