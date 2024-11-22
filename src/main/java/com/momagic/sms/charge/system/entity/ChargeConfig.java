package com.momagic.sms.charge.system.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "charge_config")
public class ChargeConfig {
    @Id
    private String chargeCode;
    private String operator;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

