<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>결재라인 선택</title>
    <link rel="stylesheet" href="<c:url value='/resources/css/approval.css' />?v=20260725-2">
</head>
<body class="popup-body">
<main class="popup-shell line-popup">
    <header class="popup-header">
        <div>
            <p class="eyebrow">Approval Line</p>
            <h1>결재라인 선택</h1>
        </div>
    </header>

    <section class="action-panel">
        <div class="line-list">
            <c:forEach var="line" items="${approvalLines}">
                <label class="line-option">
                    <input type="checkbox"
                           name="approver"
                           value="${line.approverId}"
                           data-label="${line.lineName} - ${line.description} (${line.approverId})">
                    <span>
                        <strong>${line.lineName}</strong>
                        <small>${line.description} / ${line.approverId}</small>
                    </span>
                </label>
            </c:forEach>
        </div>

        <div class="actions">
            <button id="closeLinePopupButton" type="button" class="secondary-button">닫기</button>
            <button id="applyLinePopupButton" type="button" class="primary-button">선택 적용</button>
        </div>
    </section>
</main>

<script>
    var fallbackReturnUrl = '${returnUrl}';

    var selectedApproverIds = '${selectedApproverIds}'.split(',').map(function (value) {
        return value.trim();
    }).filter(Boolean);

    document.querySelectorAll('input[name="approver"]').forEach(function (checkbox) {
        checkbox.checked = selectedApproverIds.includes(checkbox.value);
    });

    window.applyApprovalLine = function () {
        var checked = Array.from(document.querySelectorAll('input[name="approver"]:checked'));
        if (checked.length === 0) {
            alert('결재자를 1명 이상 선택하세요.');
            return;
        }

        var ids = checked.map(function (checkbox) {
            return checkbox.value;
        });
        var labels = checked.map(function (checkbox) {
            return checkbox.dataset.label;
        });
        var idsText = ids.join(',');
        var labelsText = labels.join(' > ');

        if (window.opener && !window.opener.closed && window.opener.applyApprovalLineSelection) {
            window.opener.applyApprovalLineSelection(idsText, labelsText);
            localStorage.setItem('approvalLineSelection', JSON.stringify({
                ids: idsText,
                labels: labelsText,
                appliedAt: Date.now()
            }));
            window.close();
            return;
        }

        var targetUrl = new URL(fallbackReturnUrl || '<c:url value="/approval" />', window.location.origin);
        targetUrl.searchParams.set('approverIds', idsText);
        window.location.href = targetUrl.toString();
    };

    document.getElementById('closeLinePopupButton').addEventListener('click', function () {
        if (window.opener && !window.opener.closed) {
            window.close();
            return;
        }
        window.location.href = '<c:url value="/approval" />';
    });

    document.getElementById('applyLinePopupButton').addEventListener('click', window.applyApprovalLine);
</script>
</body>
</html>
