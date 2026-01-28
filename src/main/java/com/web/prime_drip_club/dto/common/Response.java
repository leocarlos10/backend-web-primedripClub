package com.web.prime_drip_club.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Response<T> {
    private int responseCode;
    private Boolean success;
    private T data;
    private String message;
    
}
