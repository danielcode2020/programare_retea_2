package com.example.programare_retea2.controller;

import com.example.programare_retea2.service.MailViewDto;
import com.example.programare_retea2.service.MdMailService;
import jakarta.mail.MessagingException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;

@RestController
public class MailController {
    private final MdMailService mdMailService;

    public MailController(MdMailService mdMailService) {
        this.mdMailService = mdMailService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE,value = "/mail-with-attachment")
    public void sendMailWithAttachments(@RequestParam String email, @RequestParam String password,
                        @RequestParam String to, @RequestParam String subject, @RequestParam String content,
                         @RequestParam List<MultipartFile> multipartFileList){
        mdMailService.sendEmail(email, password, to, subject, content, multipartFileList);
    }

    @PostMapping("/mail-simple")
    public void sendSimple(@RequestParam String email, @RequestParam String password,
                           @RequestParam String to, @RequestParam String subject, @RequestParam String content){
        mdMailService.sendEmail(email, password, to, subject, content, Collections.emptyList());
    }

    @GetMapping("/myInbox")
    public ResponseEntity<List<MailViewDto>> readInbox(@RequestParam String email, @RequestParam String password) throws MessagingException {
        return ResponseEntity.ok().body(mdMailService.readInbox(email, password));
    }
}
