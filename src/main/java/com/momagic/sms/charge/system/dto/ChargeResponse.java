package com.momagic.sms.charge.system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class ChargeResponse {

    private int statusCode;
    private String message;
    private String transactionId;
    private String operator;
    private String shortCode;
    private String msisdn;
    private String chargeCode;

}
