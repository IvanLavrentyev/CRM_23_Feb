package com.ewp.crm.models.dto;

import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class AttachmentDto {

    private long dateSentMills;
    private File attachmentFile;
    private String content;

    public AttachmentDto() {
    }

    public AttachmentDto(long dateSentMills, File attachmentFile) {
        this.dateSentMills = dateSentMills;
        this.attachmentFile = attachmentFile;
    }

    public AttachmentDto(long dateSentMills, File attachmentFile, String content) {
        this.dateSentMills = dateSentMills;
        this.attachmentFile = attachmentFile;
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getDateSentMills() {
        return dateSentMills;
    }

    public void setDateSentMills(long dateSentMills) {
        this.dateSentMills = dateSentMills;
    }

    public File getAttachmentFile() {
        return attachmentFile;
    }

    public void setAttachmentFile(File attachmentFile) {
        this.attachmentFile = attachmentFile;
    }
}

