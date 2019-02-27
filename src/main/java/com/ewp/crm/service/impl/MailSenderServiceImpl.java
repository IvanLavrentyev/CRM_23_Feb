package com.ewp.crm.service.impl;

import com.ewp.crm.models.Client;
import com.ewp.crm.service.interfaces.ClientService;
import com.ewp.crm.service.interfaces.MailSenderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileTypeMap;
import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.util.ByteArrayDataSource;
import javax.servlet.ServletContext;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@PropertySource(value = "classpath:application.properties")
public class MailSenderServiceImpl implements MailSenderService {

    private String sendMailFrom;
    private String mailPassword;
    private Environment environment;
    private ClientService clientService;
    private ServletContext servletContext;
    private static Logger logger = LoggerFactory.getLogger(MailSenderService.class);

    @Autowired
    public MailSenderServiceImpl(ClientService clientService,
                                 Environment environment,
                                 ServletContext servletContext) {
        this.environment = environment;
        this.clientService = clientService;
        this.servletContext = servletContext;
        checkConfig(environment);
    }

    private void checkConfig(Environment environment) {
        this.sendMailFrom = environment.getRequiredProperty("spring.mail.username");
        this.mailPassword = environment.getRequiredProperty("spring.mail.password");
        if (sendMailFrom.isEmpty() || mailPassword.isEmpty()) {
            try {
                throw new NullPointerException();
            } catch (NullPointerException | IllegalStateException e) {
                logger.error("Failed to read mailbox config data in mailSenderServiceImpl for reason " + e);
                System.exit(-1);
            }
        }
    }

    @Override
    public boolean sendMessage(long id, String mailContent, MultipartFile[] files) {
        Client client = clientService.get(id);
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        logger.info("Sending email to " + client.getFullName() + " , sender " + userName);
        String mailSendTo = client.getEmail();
        String mailServerSmtpHost = environment.getRequiredProperty("spring.mail.host");
        String mailSmtpAuth = environment.getRequiredProperty("spring.mail.properties.mail.smtp.auth");
        String starttlsEnable = environment.getRequiredProperty("spring.mail.properties.mail.smtp.starttls.enable");
        String SMTPport = environment.getRequiredProperty("spring.mail.properties.mail.smtp.port");

        Properties property = System.getProperties();
        property.setProperty("mail.smtp.host", mailServerSmtpHost);
        property.setProperty("mail.smtp.port", SMTPport);
        property.setProperty("mail.smtp.auth", mailSmtpAuth);
        property.setProperty("mail.smtp.starttls.enable", starttlsEnable);

        Authenticator authenticator = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(sendMailFrom, mailPassword);
            }
        };
        Session session = Session.getInstance(property, authenticator);
        try {
            MimeMessage mimeMessage = new MimeMessage(session);
            mimeMessage.setHeader("Content-type", "text/HTML; charset=UTF-8");
            mimeMessage.setHeader("format", "flowed");
            mimeMessage.setHeader("Content-Transfer-Encoding", "8bit");
            mimeMessage.setFrom(new InternetAddress(sendMailFrom));
            mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(mailSendTo));
            mimeMessage.setSubject("Привет, " + client.getName() + " это JavaMentor!", "UTF-8");

            MimeBodyPart content = new MimeBodyPart();
            content.setText(removeHTMLtags(mailContent));
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(content);

            if (files.length != 0) {
                for (MultipartFile multipartFile : files) {
                    BodyPart bodyPart = new MimeBodyPart();

                    String mimeType = FileTypeMap.getDefaultFileTypeMap().getContentType(multipartFile.getOriginalFilename());
                    DataSource dataSource = new ByteArrayDataSource(multipartFile.getBytes(), mimeType);
                    bodyPart.setDataHandler(new DataHandler(dataSource));
                    bodyPart.setFileName(multipartFile.getOriginalFilename());
                    bodyPart.setDisposition(Part.ATTACHMENT);
                    multipart.addBodyPart(bodyPart);
                }
            }
            mimeMessage.setContent(multipart);
            logger.info("Attempt to send email to {}, sender {}", client.getFullName(), userName);
            Transport.send(mimeMessage);
            logger.info("Sent");
            return true;
        } catch (AddressException e) {
            logger.error("Wrong email address {} of {}. Reason - {}", client.getEmail(), client.getFullName(), e);
        } catch (MessagingException e) {
            logger.error("Attempt to send email to {} failed. Reason - {}", client.getFullName(), e);
        } catch (IOException e) {
            logger.error("IO exception for reason " + e);
        }
        return false;
    }

    private static String removeHTMLtags(String mailText) {
        Pattern pattern = Pattern.compile("</?.+?>\\n?");
        Matcher matcher = pattern.matcher(mailText);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, "");
        }
        return sb.toString();
    }

    private String downloadFile(MultipartFile file) {
        String localPath = environment.getRequiredProperty("email.localfolder.send.attachment")
                + file.getOriginalFilename();
        File f = new File(localPath);
        try {
            OutputStream outputStream = new FileOutputStream(f);
            outputStream.write(file.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return localPath;
    }
}