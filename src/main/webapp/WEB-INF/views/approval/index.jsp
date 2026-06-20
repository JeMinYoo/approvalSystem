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
        <button class="primary-button" type="button" onclick="openApprovalPopup()">
            신규
        </button>
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

    <section class="workspace workspace-list-only">
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
                    </tr>
                    </thead>
                    <tbody>
                    <c:choose>
                        <c:when test="${fn:length(documents) == 0}">
                            <tr>
                                <td colspan="6" class="empty-cell">등록된 결재 문서가 없습니다.</td>
                            </tr>
                        </c:when>
                        <c:otherwise>
                            <c:forEach var="document" items="${documents}">
                                <c:url var="documentPopupUrl" value="/approval/popup">
                                    <c:param name="documentId" value="${document.documentId}" />
                                </c:url>
                                <tr>
                                    <td>${document.documentId}</td>
                                    <td>
                                        <a class="title-link" href="${documentPopupUrl}" target="approvalPopup" onclick="openApprovalPopup('${document.documentId}'); return false;">
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
</script>
</body>
</html>
