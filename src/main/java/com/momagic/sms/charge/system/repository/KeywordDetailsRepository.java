package com.momagic.sms.charge.system.repository;

import com.momagic.sms.charge.system.entity.KeywordDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KeywordDetailsRepository extends JpaRepository<KeywordDetails, String> {

}
