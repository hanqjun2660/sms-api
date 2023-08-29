package com.api.smsapi.service;

import com.api.smsapi.dto.MessageDTO;
import com.api.smsapi.dto.SmsRequestDTO;
import com.api.smsapi.dto.SmsResponseDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@PropertySource("classpath:application.properties")
@Slf4j
@RequiredArgsConstructor
@Service
public class SmsService {

    private final String smsConfirmNum = createSmsKey();

    @Value(("${naver-cloud-sms.accessKey}"))
    private String accessKey;

    @Value(("${naver-cloud-sms.secretKey}"))
    private String secretKey;

    @Value(("${naver-cloud-sms.serviceId}"))
    private String serviceId;

    @Value(("${naver-cloud-sms.senderPhone}"))
    private String phone;

    // 암호화가 필요한 요청 헤더 작성을 위한 메서드 (참고 : https://api.ncloud-docs.com/docs/common-ncpapi)
    public String getSignature(String time) throws NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeyException {
        String space = " ";
        String newLine = "\n";
        String method = "POST";
        String url = "/sms/v2/services/" + this.serviceId + "/messages";
        String accessKey = this.accessKey;
        String secretKey = this.secretKey;

        String message = new StringBuilder()
                .append(method)
                .append(space)
                .append(url)
                .append(newLine)
                .append(time)
                .append(newLine)
                .append(accessKey)
                .toString();

        SecretKeySpec signingKey = new SecretKeySpec(secretKey.getBytes("UTF-8"), "HmacSHA256");
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(signingKey);

        byte[] rawHmac = mac.doFinal(message.getBytes("UTF-8"));
        String encodeBase64String = Base64.encodeBase64String(rawHmac);

        return encodeBase64String;
    }
    
    // 메세지 발송
    public SmsResponseDTO sendSms(MessageDTO messageDTO) throws UnsupportedEncodingException, NoSuchAlgorithmException, JsonProcessingException, URISyntaxException, InvalidKeyException {

        String time = Long.toString(System.currentTimeMillis());

        // 요청을 위한 헤더 준비가 끝났다.
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-ncp-apigw-timestamp", time);
        headers.set("x-ncp-iam-access-key", accessKey);
        headers.set("x-ncp-apigw-signature-v2", getSignature(time));

        // 보낼 메세지를 담아주자
        List<MessageDTO> messages = new ArrayList<>();
        messages.add(messageDTO);

        SmsRequestDTO request = SmsRequestDTO.builder()
                .type("SMS")
                .contentType("COMM")
                .countryCode("82")
                .from(phone)
                .content("[서비스 인증번호] 인증번호 [" + smsConfirmNum + "]를 입력해주세요.")
                .messages(messages)
                .build();

        // 바디에 담을 json으로 반환하자
        ObjectMapper objectMapper = new ObjectMapper();
        String body = objectMapper.writeValueAsString(request);

        // 바디랑 헤더랑 합치자
        HttpEntity<String> httpBody = new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
        //restTemplate로 post요청 보내고 오류가 없으면 202코드 반환
        SmsResponseDTO smsResponseDTO = restTemplate.postForObject(new URI("https://sens.apigw.ntruss.com/sms/v2/services/"+ serviceId +"/messages"), httpBody, SmsResponseDTO.class);
        SmsResponseDTO responseDTO = new SmsResponseDTO();
        responseDTO.setSmsConfirmNum(smsConfirmNum);

        return smsResponseDTO;
    }

    // 인증코드 생성
    private String createSmsKey() {
        StringBuffer key = new StringBuffer();
        Random rnd = new Random();

        for(int i = 0; i < 5; i++) {
            key.append((rnd.nextInt(10)));
        }

        return key.toString();
    }

}
