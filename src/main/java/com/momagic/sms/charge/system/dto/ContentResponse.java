package com.momagic.sms.charge.system.dto;

import com.momagic.sms.charge.system.dto.Content;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Builder
@Accessors(chain = true)
public class ContentResponse {

    private int statusCode;
    private String message;
    private int contentCount;
    private List<Content> contents;

}
