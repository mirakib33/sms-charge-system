package com.momagic.sms.charge.system.repository;

import com.momagic.sms.charge.system.entity.ChargeSuccessLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChargeSuccessLogRepository extends JpaRepository<ChargeSuccessLog, Integer> {}
