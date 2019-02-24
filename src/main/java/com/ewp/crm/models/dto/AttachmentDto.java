package com.ewp.crm.models.dto;

import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class AttachmentDto {

    private long DateSentMills;
    private File attachmentFile;

    public AttachmentDto() {
    }

    public AttachmentDto(long dateSentMills, File attachmentFile) {
        DateSentMills = dateSentMills;
        this.attachmentFile = attachmentFile;
    }

    public long getDateSentMills() {
        return DateSentMills;
    }

    public void setDateSentMills(long dateSentMills) {
        DateSentMills = dateSentMills;
    }

    public File getAttachmentFile() {
        return attachmentFile;
    }

    public void setAttachmentFile(File attachmentFile) {
        this.attachmentFile = attachmentFile;
    }
}

