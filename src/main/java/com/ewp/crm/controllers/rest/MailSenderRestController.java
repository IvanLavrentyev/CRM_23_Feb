package com.ewp.crm.controllers.rest;

import com.ewp.crm.service.interfaces.MailSenderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class MailSenderRestController {

    private MailSenderService mailSenderService;

    @Autowired
    public MailSenderRestController(MailSenderService mailSenderService) {
        this.mailSenderService = mailSenderService;
    }

    @PreAuthorize("hasAnyAuthority('OWNER', 'ADMIN')")
    @PostMapping(value = "/emails/send")
    public ResponseEntity sendEmail(@RequestParam("id") long id,
                                    @RequestParam ("mailContent") String mailContent,
                                    @RequestParam ("attachments") MultipartFile[] files){
        return ResponseEntity.ok(mailSenderService.sendMessage(id, mailContent, files));
    }
}
