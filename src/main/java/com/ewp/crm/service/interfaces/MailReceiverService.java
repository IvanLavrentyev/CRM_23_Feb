package com.ewp.crm.service.interfaces;

import com.ewp.crm.models.dto.MailDto;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface MailReceiverService {

    List<MailDto> getAllUnreadEmailsFor(Long userId);

    List<Long> checkMessagesInGMailInbox();

    void downLoadAttachment(String fileName, String userName, HttpServletResponse response);
}
