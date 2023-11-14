package com.api.smsapi.service;

import com.api.smsapi.config.SmsKeyGenerator;
import com.api.smsapi.config.SmsProperties;
import com.api.smsapi.dto.MessageDTO;
import com.api.smsapi.dto.SmsRequestDTO;
import com.api.smsapi.dto.SmsResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class SmsService {

    private final ObjectMapper objectMapper;
    private final SmsProperties smsProperties;

    public SmsService(ObjectMapper objectMapper, SmsProperties smsProperties) {
        this.objectMapper = objectMapper;
        this.smsProperties = smsProperties;
    }

    // 암호화가 필요한 요청 헤더 작성을 위한 메서드 (참고 : https://api.ncloud-docs.com/docs/common-ncpapi)
    @SneakyThrows
    public String getSignature(String time)  {
        char space = ' ';
        char newLine = '\n';
        String method = "POST";
        String url = String.format("/sms/v2/services/%s/messages", smsProperties.getServiceId());

        String message = new StringBuilder()
                .append(method)
                .append(space)
                .append(url)
                .append(newLine)
                .append(time)
                .append(newLine)
                .append(smsProperties.getAccessKey())
                .toString();

        SecretKeySpec signingKey = new SecretKeySpec(
                smsProperties.getSecretKey().getBytes("UTF-8"),
                "HmacSHA256"
        );
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(signingKey);

        byte[] rawHmac = mac.doFinal(message.getBytes("UTF-8"));
        return Base64.encodeBase64String(rawHmac);
    }
    
    // 메세지 발송
    public SmsResponseDTO sendSms(MessageDTO messageDTO) throws Exception {

        // 요청을 위한 헤더 준비가 끝났다.
        HttpHeaders headers = setHttpHeaders();

        // 인증코드 생성
        final String smsKey = SmsKeyGenerator.generateSmsKey();

        // 보낼 메세지를 담아주자
        List<MessageDTO> messages = new ArrayList<>();
        messages.add(messageDTO);

        SmsRequestDTO request = SmsRequestDTO.builder()
                .type("SMS")
                .contentType("COMM")
                .countryCode("82")
                .from(smsProperties.getSenderPhone())
                .content("[서비스 인증번호] 인증번호 [" + smsKey + "]를 입력해주세요.")
                .messages(messages)
                .build();

        // 바디에 담을 json으로 반환하자
        String body = objectMapper.writeValueAsString(request);

        // 바디랑 헤더랑 합치자
        HttpEntity<String> httpBody = new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
        //restTemplate로 post요청 보내고 오류가 없으면 202코드 반환
        SmsResponseDTO smsResponseDTO = restTemplate.postForObject(
                new URI("https://sens.apigw.ntruss.com/sms/v2/services/"+ smsProperties.getServiceId() +"/messages"),
                httpBody,
                SmsResponseDTO.class
        );

        SmsResponseDTO responseDTO = new SmsResponseDTO();
        responseDTO.setSmsConfirmNum(smsKey);

        return smsResponseDTO;
    }


    private HttpHeaders setHttpHeaders()  {
        String currentTime = Long.toString(System.currentTimeMillis());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-ncp-apigw-timestamp", currentTime);
        headers.set("x-ncp-iam-access-key", smsProperties.getAccessKey());
        headers.set("x-ncp-apigw-signature-v2", getSignature(currentTime));
        return headers;
    }

}
