package com.ewp.crm.controllers;

import com.ewp.crm.service.interfaces.ClientService;
import com.ewp.crm.service.interfaces.MailReceiverService;
import com.ewp.crm.service.interfaces.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.HttpServletResponse;

@Controller
@PropertySource("classpath:application.properties")
public class EmailsPageController {

    private String userName;
    private String clientName;
    private Environment environment;
    private UserService userService;
    private ClientService clientService;
    private MailReceiverService mailReceiverService;
    private static Logger logger = LoggerFactory.getLogger(EmailsPageController.class);

    @Autowired
    public EmailsPageController(Environment environment,
                                UserService userService,
                                ClientService clientService,
                                MailReceiverService mailReceiverService) {

        this.environment = environment;
        this.userService = userService;
        this.clientService = clientService;
        this.mailReceiverService = mailReceiverService;
    }

    @PreAuthorize("hasAnyAuthority('OWNER', 'ADMIN')")
    @GetMapping(value = "/emails/{id}")
    public String getEmails(@PathVariable("id") Long id) {
        clientName = clientService.get(id).getName() + " " + clientService.get(id).getLastName();
        userName = SecurityContextHolder.getContext().getAuthentication().getName();
        logger.info("Reading emails from " + clientName + " by " + userName);
        return "emailPage";
    }

    @PreAuthorize("hasAnyAuthority('OWNER', 'ADMIN')")
    @GetMapping(value = "/emails/downLoad/{fileName}")
    public void downLoadAttachment(@PathVariable("fileName") String fileName, HttpServletResponse response){
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        logger.info("Getting attachments for " + clientName + " by " + userName);
        mailReceiverService.downLoadAttachment(fileName, userName, response);
    }

}
