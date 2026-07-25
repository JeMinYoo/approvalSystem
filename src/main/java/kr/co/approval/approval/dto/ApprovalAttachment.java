package kr.co.approval.approval.dto;

import java.util.Arrays;

public class ApprovalAttachment {

    private final String attachmentId;
    private final String fileName;
    private final String contentType;
    private final byte[] content;

    public ApprovalAttachment(String attachmentId, String fileName, String contentType, byte[] content) {
        this.attachmentId = attachmentId;
        this.fileName = fileName;
        this.contentType = contentType;
        this.content = Arrays.copyOf(content, content.length);
    }

    public String getAttachmentId() {
        return attachmentId;
    }

    public String getFileName() {
        return fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public long getSize() {
        return content.length;
    }

    public byte[] getContent() {
        return Arrays.copyOf(content, content.length);
    }
}
