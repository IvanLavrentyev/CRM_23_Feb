package com.ewp.crm.controllers.rest;

import com.ewp.crm.models.dto.AttachmentDto;
import com.ewp.crm.models.dto.MailDto;
import com.ewp.crm.service.interfaces.MailReceiverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.List;

@RestController
public class MailRecieverRestController {

    private MailReceiverService mailReceiverService;

    @Autowired
    public MailRecieverRestController(MailReceiverService mailReceiverService) {
        this.mailReceiverService = mailReceiverService;
    }

    @PreAuthorize("hasAnyAuthority('OWNER', 'ADMIN')")
    @PostMapping(value = "/mail/getEmails")
    public ResponseEntity<List<MailDto>> getAllUnreadEmailsFor(@RequestParam(name = "id") Long id){
        List<MailDto> mailList = mailReceiverService.getAllEmailsFor(id);
        return ResponseEntity.ok(mailList);
    }

    @PreAuthorize("hasAnyAuthority('OWNER', 'ADMIN')")
    @PostMapping(value = "/mail/getAttachments")
    public ResponseEntity<List<AttachmentDto>> getAllAttachments(@RequestParam(name = "sentDateMills") Long sentDateMills) {
        List<AttachmentDto> attachmentsList = mailReceiverService.getAttachmentsFromEmail(sentDateMills);
        return ResponseEntity.ok(attachmentsList);
    }
}
