package com.momagic.sms.charge.system.repository;

import com.momagic.sms.charge.system.entity.Inbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InboxRepository extends JpaRepository<Inbox, Integer> {

    @Query(value = "SELECT * FROM inbox i WHERE i.status = :status ORDER BY i.created_at DESC LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<Inbox> findAllByStatus(@Param("status") String status, @Param("limit") Integer limit, @Param("offset") Integer offset);

}
