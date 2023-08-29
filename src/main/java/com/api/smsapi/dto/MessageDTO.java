package com.api.smsapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class MessageDTO {
    // 받을사람
    private String to;
    // 메세지 내용
//    private String Content;

}
