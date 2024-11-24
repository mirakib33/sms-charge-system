package com.momagic.sms.charge.system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class ContentResponse {

    private int statusCode;
    private String message;
    private int contentCount;
    private List<Content> contents;

}
