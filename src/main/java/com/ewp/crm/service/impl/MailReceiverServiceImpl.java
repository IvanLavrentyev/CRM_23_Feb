package com.ewp.crm.service.impl;

import com.ewp.crm.models.Client;
import com.ewp.crm.models.dto.MailDto;
import com.ewp.crm.service.interfaces.ClientService;
import com.ewp.crm.service.interfaces.MailReceiverService;
import com.ewp.crm.service.interfaces.UserService;
//        import com.ewp.crm.utils.cleaner.TransitFolderCleaner;
import com.ewp.crm.utils.cleaner.TransitFolderCleaner;
import com.google.api.client.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@PropertySource("classpath:application.properties")

public class MailReceiverServiceImpl implements MailReceiverService {

    private static Logger logger = LoggerFactory.getLogger(MailReceiverServiceImpl.class);

    private String eMailServerHost;
    private String eMailLogin;
    private String eMailPassword;
    private Environment environment;
    private UserService userService;
    private MailDto mailDto;
    private ClientService clientService;
    private String localFolder;
    private List<Message> messages;

    @Autowired
    public MailReceiverServiceImpl(Environment environment,
                                   UserService userService,
                                   MailDto mailDto,
                                   ClientService clientService) {
        checkConfig(environment);
        this.clientService = clientService;
        this.mailDto = mailDto;
        this.userService = userService;
        this.environment = environment;
    }

    private void checkConfig(Environment environment) {
        try {
            this.eMailServerHost = environment.getRequiredProperty("spring.mail.host");
            this.eMailLogin = environment.getRequiredProperty("spring.mail.username");
            this.eMailPassword = environment.getRequiredProperty("spring.mail.password");
            this.localFolder = environment.getRequiredProperty("email.localfolder");

            if (eMailServerHost.isEmpty() || eMailLogin.isEmpty() || eMailPassword.isEmpty())
                throw new NullPointerException();

        } catch (IllegalStateException | NullPointerException e) {
            logger.error("Mail configs failed to get initialized in MailReceiverService. Check application.properties file");
            System.exit(-1);
        }
    }

    @Override
    public List<Long> checkMessagesInGMailInbox() {

        List<Long> userIdList = new ArrayList<>();

        if (messages != null) {
            for (Message message : messages) {
                try {
                    if (!message.isSet(Flags.Flag.SEEN)) {
                        String email = getEmailAddress(message.getFrom()[0].toString());
                        try {
                            long clientId = clientService.getClientByEmail(email).getId();
                            userIdList.add(clientId);
                            message.setFlag(Flags.Flag.SEEN, false);
                        } catch (NullPointerException e) {
                            logger.error("Email from unknown address {} has been received ", email);
                            message.setFlag(Flags.Flag.SEEN, false);
                        }
                    }
                } catch (MessagingException e) {
                    logger.error("Messaging exception" + e);
                }
            }
            return userIdList;
        }
        logger.error("List of messages is null in checkMessagesInGMailInbox()");
        return null;
    }

    @Override
    public void downLoadAttachment(String fileName, String userName, HttpServletResponse response) {
        String FILES_DIR = environment.getRequiredProperty("email.localfolder");
        File file = new File(FILES_DIR + File.pathSeparator + fileName);

        if (!file.exists())
            try {
                throw new FileNotFoundException("File " + fileName + " doesn't exist");
            } catch (FileNotFoundException e) {
                logger.error("Attempt to get attachments for " + userName + " failed, folder " + fileName +
                        " doesn't exist");
            }
        try{
            InputStream in = new FileInputStream(FILES_DIR + fileName);
            response.setContentType("application/force-download");
            response.setHeader("Content-Disposition", "attachment; filename="+fileName);
            IOUtils.copy(in, response.getOutputStream());
            response.flushBuffer();
            in.close();

        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    @Override
    public List<MailDto> getAllEmailsFor(Long id) {
        TransitFolderCleaner.cleanFolder(localFolder);
        List<MailDto> messageList = new ArrayList<>();
        Client client = clientService.get(id);

        messages = Arrays.asList(getMessages("imaps", "imap.gmail.com", eMailLogin,
                eMailPassword, "INBOX"));

       messages.forEach(message -> {
           try {
               String from = message.getFrom()[0].toString();
               if (client.getEmail().equalsIgnoreCase(getEmailAddress(from))){
                   MailDto mail = new MailDto();
                   mail.setSeen(message.isSet(Flags.Flag.SEEN));
                   mail.setSentDate(convertDate(message.getSentDate()));
                   mail.setSentDateMills(message.getSentDate().getTime());
                   mail.setSubject(message.getSubject());
                   messageList.add(mail);
               }else
                   message.setFlag(Flags.Flag.SEEN, false);

           } catch (MessagingException e) {
               e.printStackTrace();
           }
       });
        Collections.reverse(messageList);
        return messageList;
    }

//    @Override
//    public List<MailDto> getAttachmentsFromEmail (long sentDateMills){
//        List<MailDto> attachments = new ArrayList<>();
//        messages.forEach(message -> {
//            try {
//                if (message.getSentDate().getTime() == sentDateMills){
//                    String messageText = getTextFromMessage(message);
//                    Multipart multipart = (Multipart) message.getContent();
//                    MailDto mailDto = new MailDto();
//                    mailDto.setContent(messageText);
//                    for (int i = 0; i < multipart.getCount(); i++) {
//                        BodyPart bodyPart = multipart.getBodyPart(i);
//                        if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())){
//                            File file = new File("transitFolder/receivedAttachments/" + bodyPart.getFileName());
//                            ((MimeBodyPart) bodyPart).saveFile(file);
//                            attachments.add(file);
//                        }
//                    }
//                }
//            } catch (MessagingException | IOException e) {
//                e.printStackTrace();
//            }
//        });
//        return attachments;
//    }

    @Override
    public MailDto getEmailContentAndAttachment (long sentDateMills){
        TransitFolderCleaner.cleanFolder(localFolder);
        List<File> attachments = new ArrayList<>();
        MailDto mail = new MailDto();
        messages.forEach(message -> {
            try {
                if (message.getSentDate().getTime() == sentDateMills){
                    String messageText = getTextFromMessage(message);
                    Multipart multipart = (Multipart) message.getContent();
                    mail.setContent(messageText);

                    for (int i = 0; i < multipart.getCount(); i++) {
                        BodyPart bodyPart = multipart.getBodyPart(i);
                        if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())){
                            File attachment = new File("transitFolder/receivedAttachments/" + bodyPart.getFileName());
                            ((MimeBodyPart) bodyPart).saveFile(attachment);
                            attachments.add(attachment);
                        }
                    }
                }
            } catch (MessagingException | IOException e) {
                e.printStackTrace();
            }
        });
        mail.setAttachments(attachments);
        return mail;
    }

    private String getTextFromMessage(Message message) throws MessagingException, IOException {
        String result = "";
        if (message.isMimeType("text/plain")) {
            result = message.getContent().toString();
        } else if (message.isMimeType("multipart/*")) {
            MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
            result = getTextFromMimeMultipart(mimeMultipart);
        }
        return result;
    }

    private String getTextFromMimeMultipart(MimeMultipart mimeMultipart) throws MessagingException, IOException {
        String result = "";
        int count = mimeMultipart.getCount();
        for (int i = 0; i < count; i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain")) {
                result = result + "\n" + bodyPart.getContent();
                break;
            } else if (bodyPart.isMimeType("text/html")) {
                String html = (String) bodyPart.getContent();
                result = result + "\n" + org.jsoup.Jsoup.parse(html).text();
            } else if (bodyPart.getContent() instanceof MimeMultipart) {
                result = result + getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent());
            }
        }
        return result;
    }

//    private List<File> getAttachmentsFromMessage (Message message, int j) throws IOException, MessagingException {
//        List<File> attachments = new ArrayList<>();
//        Multipart multipart = (Multipart) message.getContent();
//        for (int i = 0; i < multipart.getCount() ; i++) {
//            BodyPart bodyPart = multipart.getBodyPart(i);
//            if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())){
//                File file = new File(localFolder + j +"_"+ bodyPart.getFileName());
//                ((MimeBodyPart) bodyPart).saveFile(file);
//                attachments.add(file);
//            }
//        }
//        return attachments;
//    }



    private String convertDate(Date date) {
        Format formatter = new SimpleDateFormat("dd-MM-yyyy");
        return formatter.format(date);
    }

    private String getEmailAddress(String email) {
        return email.substring(email.indexOf("\" <") + 3, email.lastIndexOf(">"));
    }

    private Message[] getMessages(String protocol, String host, String eMailLogin,
                                  String eMailPassword, String mailboxToOpen) {
        Store store;
        Folder inbox;
        Message[] messages = null;
        Properties properties = new Properties();

        properties.put("mail.imap.partialfetch","false");
        properties.put("mail.imap.fetchsize", "3000000");
        properties.put("mail.imaps.partialfetch", "false");
        properties.put("mail.imaps.fetchsize", "3000000");

        Session session = Session.getDefaultInstance(properties, null);

        try {
            store = session.getStore(protocol);
            store.connect(host, eMailLogin, eMailPassword);
            inbox = store.getFolder(mailboxToOpen);
            inbox.open(Folder.READ_WRITE);

            FetchProfile fp = new FetchProfile();
            fp.add(FetchProfile.Item.ENVELOPE);
            fp.add("Subject");

            messages = inbox.getMessages();
            inbox.fetch(messages, fp);

        } catch (NoSuchProviderException e) {
            logger.error("Failed to connect to remote server, wrong protocol" + e);
        } catch (MessagingException e) {
            logger.error("Messaging exception in getMessage()" + e);
        }
        return messages;
    }
}
