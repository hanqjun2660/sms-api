package com.api.smsapi.controller;

import com.api.smsapi.dto.MessageDTO;
import com.api.smsapi.dto.SmsResponseDTO;
import com.api.smsapi.service.SmsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SmsController {

    private final SmsService smsService;

    public SmsController(SmsService smsService) {
        this.smsService = smsService;
    }

    @PostMapping("/sms/send")
    public SmsResponseDTO sendSms(@RequestBody MessageDTO messageDTO) throws Exception {
        SmsResponseDTO response = smsService.sendSms(messageDTO);
        return response;
    }
}
