package com.api.smsapi.controller;

import com.api.smsapi.dto.MessageDTO;
import com.api.smsapi.dto.SmsResponseDTO;
import com.api.smsapi.service.SmsService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@RestController
public class SmsController {

    private SmsService smsService;

    @Autowired
    public SmsController(SmsService smsService) {
        this.smsService = smsService;
    }

    @PostMapping("/sms/send")
    public SmsResponseDTO sendSms(@RequestBody MessageDTO messageDTO) throws UnsupportedEncodingException, NoSuchAlgorithmException, URISyntaxException, JsonProcessingException, InvalidKeyException {
        SmsResponseDTO response = smsService.sendSms(messageDTO);
        return response;
    }
}
