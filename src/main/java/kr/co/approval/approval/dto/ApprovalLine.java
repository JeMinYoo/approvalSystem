package kr.co.approval.approval.dto;

public class ApprovalLine {

    private final String lineId;
    private final String lineName;
    private final String approverId;
    private final String description;

    public ApprovalLine(String lineId, String lineName, String approverId, String description) {
        this.lineId = lineId;
        this.lineName = lineName;
        this.approverId = approverId;
        this.description = description;
    }

    public String getLineId() {
        return lineId;
    }

    public String getLineName() {
        return lineName;
    }

    public String getApproverId() {
        return approverId;
    }

    public String getDescription() {
        return description;
    }
}
