package com.momagic.sms.charge.system.repository;

import com.momagic.sms.charge.system.entity.ChargeFailureLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChargeFailureLogRepository extends JpaRepository<ChargeFailureLog, Integer> {

}
