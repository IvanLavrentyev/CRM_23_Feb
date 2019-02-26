package com.ewp.crm.service.interfaces;

import org.springframework.web.multipart.MultipartFile;

public interface MailSenderService {

    boolean sendMessage(long id, String mailContent, MultipartFile[] files);

}
