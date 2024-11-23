package com.momagic.sms.charge.system.repository;

import com.momagic.sms.charge.system.entity.ChargeConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChargeConfigRepository extends JpaRepository<ChargeConfig, String> {

    @Query("SELECT c.chargeCode FROM ChargeConfig c WHERE c.operator = :operator")
    Optional<String> findChargeCodeByOperator(String operator);


}
