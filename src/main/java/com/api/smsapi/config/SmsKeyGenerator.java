package com.api.smsapi.config;

import java.util.Random;

public class SmsKeyGenerator {

    private static final Random random = new Random();

    private SmsKeyGenerator() { }

    // 인증코드 생성
    public static  String generateSmsKey() {
        StringBuilder key = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            key.append(random.nextInt(10));
        }
        return key.toString();
    }

}
