package com.momagic.sms.charge.system.dto;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Builder
@Accessors(chain = true)
public class ChargeRequestBody {

    private String transactionId;
    private String operator;
    private String shortCode;
    private String msisdn;
    private String chargeCode;

}
