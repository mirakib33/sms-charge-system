package com.momagic.sms.charge.system.repository;

import com.momagic.sms.charge.system.entity.ChargeConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChargeConfigRepository extends JpaRepository<ChargeConfig, Long> {

}
