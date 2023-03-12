package com.example.programare_retea2.service;

import jakarta.mail.*;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.ByteArrayDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class MdMailService {
    private final Logger log = LoggerFactory.getLogger(MdMailService.class);

    @Async
    public void sendEmail(String email, String password,String to, String subject, String content, List<MultipartFile> multipartFileList) {
        log.info(
                "Sent email to '{}' with subject '{}' and content={}",
                to,
                subject,
                content
        );

        JavaMailSenderImpl mailSender = getSender(email, password);

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        boolean isMultipart = !multipartFileList.isEmpty();
        try {
            MimeMessageHelper myMessage = new MimeMessageHelper(mimeMessage, isMultipart, StandardCharsets.UTF_8.name());
            myMessage.setTo(to);
            myMessage.setFrom(email);
            myMessage.setSubject(subject);
            myMessage.setText(content);
            multipartFileList.forEach(multipartFile -> {
                try {
                    myMessage.addAttachment(Objects.requireNonNull(multipartFile.getOriginalFilename()),new ByteArrayDataSource(multipartFile.getBytes(),multipartFile.getContentType()));
                } catch (IOException | MessagingException e) {
                    log.error(e.toString());
                    throw new RuntimeException(e);
                }
            });
            mailSender.send(mimeMessage);
            log.info("Sent email to User '{}'", to);
        } catch (MailException | MessagingException e) {
            log.error("Email could not be sent to user '{}'", to, e);
        }
    }

    private static JavaMailSenderImpl getSender(String email, String password) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.gmail.com");
        mailSender.setPort(587);

        mailSender.setUsername(email);
        mailSender.setPassword(password);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");
        return mailSender;
    }

    public List<MailViewDto> readInbox(String email, String password) throws MessagingException {
        List<MailViewDto> viewDtoList = new ArrayList<>();
        Properties seshProperties = new Properties();
        seshProperties.put("mail.store.protocol", "imaps");
        Session session = Session.getDefaultInstance(seshProperties, null);
        Store store = session.getStore("imaps");
        store.connect("imap.gmail.com",email,password);
        Folder inbox = store.getFolder("Inbox");
        inbox.open(Folder.READ_ONLY);
        Arrays.stream(inbox.getMessages()).skip(Math.max(0, inbox.getMessages().length - 5)).forEach(message -> {
            try {
                viewDtoList.add(new MailViewDto(Arrays.toString(message.getFrom()), message.getSubject()));
            } catch (MessagingException e) {
                throw new RuntimeException(e);
            }
        });
        return viewDtoList;
    }


}
