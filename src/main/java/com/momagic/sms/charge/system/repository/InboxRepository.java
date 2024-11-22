package com.momagic.sms.charge.system.repository;

import com.momagic.sms.charge.system.entity.Inbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InboxRepository extends JpaRepository<Inbox, Integer> {

}
