<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!doctype html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>결재 팝업</title>
    <link rel="stylesheet" href="<c:url value='/resources/css/approval.css' />">
</head>
<body class="popup-body">
<main class="popup-shell">
    <header class="popup-header">
        <div>
            <p class="eyebrow">Approval Popup</p>
            <h1>${createMode ? '신규 결재 문서' : '결재 처리'}</h1>
        </div>
        <c:if test="${not createMode}">
            <span class="status status-${document.status.cssName}">
                ${document.status.displayName}
            </span>
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

    <c:choose>
        <c:when test="${createMode}">
            <section class="document-view">
                <article class="content-box">
                    <h2>새 문서 작성</h2>
                    <form method="post" action="<c:url value='/approval/popup/documents' />" class="approval-form compact-form">
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
                                <input id="popupCreateApproverIds" type="hidden" name="approverIds" value="${createRequest.approverIds}">
                                <div class="line-picker">
                                    <input id="popupCreateApproverDisplay" value="${createApproverLineText}" readonly>
                                    <button type="button" class="secondary-button" onclick="openApprovalLinePopup('popupCreateApproverIds', 'popupCreateApproverDisplay')">
                                        결재라인 선택
                                    </button>
                                </div>
                            </label>
                        </div>
                        <label>
                            내용
                            <textarea name="content" rows="8" placeholder="연계 처리 대상, 요청 사유, 참고 사항을 입력하세요." required>${createRequest.content}</textarea>
                        </label>
                        <div class="actions">
                            <button type="button" class="secondary-button" onclick="refreshOpenerAndClose()">닫기</button>
                            <button type="submit" class="primary-button">문서 생성</button>
                        </div>
                    </form>
                </article>
            </section>
        </c:when>
        <c:when test="${deleted}">
            <section class="document-view">
                <article class="content-box">
                    <h2>삭제된 문서</h2>
                    <p>${document.documentId} 문서가 목록에서 삭제되었습니다.</p>
                </article>
                <div class="actions">
                    <button type="button" class="primary-button" onclick="refreshOpenerAndClose()">확인</button>
                </div>
            </section>
        </c:when>
        <c:otherwise>
            <section class="document-view">
                <dl class="summary">
                    <div>
                        <dt>문서번호</dt>
                        <dd>${document.documentId}</dd>
                    </div>
                    <div>
                        <dt>현재 상태</dt>
                        <dd>${document.status.displayName}</dd>
                    </div>
                    <div>
                        <dt>요청일시</dt>
                        <dd>${empty document.requestedAt ? '-' : document.requestedAt}</dd>
                    </div>
                    <div>
                        <dt>처리일시</dt>
                        <dd>${empty document.decidedAt ? '-' : document.decidedAt}</dd>
                    </div>
                </dl>

                <c:choose>
                    <c:when test="${document.editable}">
                        <article class="content-box">
                            <h2>문서 수정</h2>
                            <form method="post" action="<c:url value='/approval/update' />" class="approval-form compact-form">
                                <input type="hidden" name="documentId" value="${document.documentId}">
                                <label>
                                    제목
                                    <input name="title" value="${document.title}" required>
                                </label>
                                <div class="field-grid">
                                    <label>
                                        요청자
                                        <input value="${loginUserName} (${loginUserId})" readonly>
                                        <input type="hidden" name="requesterId" value="${loginUserId}">
                                    </label>
                                    <label>
                                        결재라인
                                        <input id="popupApproverIds" type="hidden" name="approverIds" value="${document.approverIdsText}">
                                        <div class="line-picker">
                                            <input id="popupApproverDisplay" value="${document.approverLineText}" readonly>
                                            <button type="button" class="secondary-button" onclick="openApprovalLinePopup('popupApproverIds', 'popupApproverDisplay')">
                                                결재라인 선택
                                            </button>
                                        </div>
                                    </label>
                                </div>
                                <label>
                                    내용
                                    <textarea name="content" rows="5" placeholder="연계 처리 대상, 요청 사유, 참고 사항을 입력하세요." required>${document.content}</textarea>
                                </label>
                                <div class="field-grid">
                                    <label>
                                        작성자
                                        <input value="${loginUserName} (${loginUserId})" readonly>
                                        <input type="hidden" name="actorId" value="${loginUserId}">
                                    </label>
                                    <label>
                                        처리 의견
                                        <input name="comment" placeholder="수정, 삭제, 상신 의견을 입력하세요.">
                                    </label>
                                </div>
                                <div class="actions">
                                    <button type="button" class="secondary-button" onclick="refreshOpenerAndClose()">닫기</button>
                                    <button type="submit" class="danger-button" formaction="<c:url value='/approval/delete' />">
                                        삭제
                                    </button>
                                    <button type="submit" class="secondary-button" formaction="<c:url value='/approval/update' />">
                                        저장
                                    </button>
                                    <button type="submit" class="primary-button" formaction="<c:url value='/approval/update-request' />">
                                        상신
                                    </button>
                                </div>
                            </form>
                        </article>
                    </c:when>
                    <c:otherwise>
                        <article class="content-box">
                            <h2>요청 내용</h2>
                            <dl class="read-grid">
                                <div>
                                    <dt>제목</dt>
                                    <dd>${document.title}</dd>
                                </div>
                                <div>
                                    <dt>요청자</dt>
                                    <dd>${document.requesterId}</dd>
                                </div>
                                <div>
                                    <dt>결재라인</dt>
                                    <dd>${document.approverLineText}</dd>
                                </div>
                                <div>
                                    <dt>현재 결재자</dt>
                                    <dd>${document.approverId} (${document.approvalStep}/${document.approvalStepCount})</dd>
                                </div>
                            </dl>
                            <p>${document.content}</p>
                        </article>
                    </c:otherwise>
                </c:choose>
            </section>

            <c:if test="${document.decidable}">
                <section class="action-panel">
                    <h2>결재자 액션</h2>
                    <form id="approvalActionForm" method="post" class="approval-form">
                        <input type="hidden" name="documentId" value="${document.documentId}">
                        <label>
                            결재자 ID
                            <input name="actorId" value="${actionRequest.actorId}" required>
                        </label>
                        <label>
                            결재 의견
                            <textarea name="comment" rows="4">${actionRequest.comment}</textarea>
                        </label>

                        <div class="actions">
                            <button type="button" class="secondary-button" onclick="refreshOpenerAndClose()">닫기</button>
                            <button type="submit" class="danger-button" formaction="<c:url value='/approval/reject' />">
                                반려
                            </button>
                            <button type="submit" class="primary-button" formaction="<c:url value='/approval/approve' />">
                                승인
                            </button>
                        </div>
                    </form>
                </section>
            </c:if>

            <section class="history-panel">
                <div class="section-title">
                    <h2>처리 이력</h2>
                    <span>${fn:length(document.histories)}건</span>
                </div>
                <ol class="history-list">
                    <c:forEach var="history" items="${document.histories}">
                        <li>
                            <strong>${history.status.displayName}</strong>
                            <span>${history.actorId}</span>
                            <p>${history.comment}</p>
                            <time>${history.processedAt}</time>
                        </li>
                    </c:forEach>
                </ol>
            </section>
        </c:otherwise>
    </c:choose>
</main>

<script>
    const approvalHomeUrl = '<c:url value="/approval" />';

    function refreshOpenerAndClose() {
        if (window.opener && !window.opener.closed) {
            window.opener.location.reload();
            window.close();
            return;
        }

        window.location.href = approvalHomeUrl;
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
