package com.ewp.crm.models.dto;

import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

@Component
public class MailDto {

    private long clientId;

    private String sentDate;

    private long sentDateMills;

    private String subject;

    private String sentFrom;

    private boolean isSeen;

    private String content;

    private List<File> attachments;

    private String localAttachmentsFolder;

//    private File attachmentFile;

    public long getClientId() {
        return clientId;
    }

    public void setClientId(long clientId) {
        this.clientId = clientId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

//    public File getAttachmentFile() {
//        return attachmentFile;
//    }
//
//    public void setAttachmentFile(File attachmentFile) {
//        this.attachmentFile = attachmentFile;
//    }



    public MailDto() {}

    public long getSentDateMills() {
        return sentDateMills;
    }

    public void setSentDateMills(long sentDateMills) {
        this.sentDateMills = sentDateMills;
    }

    public long getUserId() {
        return clientId;
    }

    public void setUserId(long userId) {
        this.clientId = userId;
    }

    public String getSentDate() {
        return sentDate;
    }

    public void setSentDate(String sentDate) {
        this.sentDate = sentDate;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getSentFrom() {
        return sentFrom;
    }

    public void setSentFrom(String sentFrom) {
        this.sentFrom = sentFrom;
    }

    public boolean isSeen() {
        return isSeen;
    }

    public void setSeen(boolean seen) {
        isSeen = seen;
    }

    public List<File> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<File> attachments) {
        this.attachments = attachments;
    }

    public String getLocalAttachmentsFolder() {
        return localAttachmentsFolder;
    }

    public void setLocalAttachmentsFolder(String localAttachmentsFolder) {
        this.localAttachmentsFolder = localAttachmentsFolder;
    }

    @Override
    public String toString() {
        return "MailDto{" +
                "userId=" + clientId +
                ", sentDate='" + sentDate + '\'' +
                ", subject='" + subject + '\'' +
                ", sentFrom='" + sentFrom + '\'' +
                '}';
    }
}

