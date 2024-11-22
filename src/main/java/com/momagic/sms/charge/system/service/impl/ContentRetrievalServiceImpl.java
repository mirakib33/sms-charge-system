package com.momagic.sms.charge.system.service.impl;

import com.momagic.sms.charge.system.dto.Content;
import com.momagic.sms.charge.system.dto.ContentResponse;
import com.momagic.sms.charge.system.entity.Inbox;
import com.momagic.sms.charge.system.repository.InboxRepository;
import com.momagic.sms.charge.system.service.ContentRetrievalService;
import com.momagic.sms.charge.system.utils.AppConstants;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ContentRetrievalServiceImpl implements ContentRetrievalService {

    private final InboxRepository inboxRepository;

    private final RestTemplate restTemplate;

    @Value("${api.content.url}")
    private String contentUrl;

//    @PostConstruct
    public void init() {
        System.out.println("Fetching and inserting content...");
        fetchAndInsertContent();
    }

    @Override
    public void fetchAndInsertContent() {
        try {
            ResponseEntity<ContentResponse> response = restTemplate.getForEntity(contentUrl, ContentResponse.class);

            if (response != null && response.getStatusCode() == HttpStatus.OK) {
                ContentResponse contentResponse = response.getBody();
                if (contentResponse != null && !contentResponse.getContents().isEmpty()) {
                    for (Content content : contentResponse.getContents()) {
                        inboxRepository.save(mapToInbox(content));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error fetching content: " + e.getMessage());
        }
    }

    private Inbox mapToInbox(Content content) {
        Inbox inbox = new Inbox();
        String[] parts = content.getSms().split(" ");
        if (parts.length >= 4) {
            inbox.setKeyword(parts[0]);
            inbox.setGameName(parts[1]);
        }
        inbox.setTransactionId(content.getTransactionId());
        inbox.setOperator(content.getOperator());
        inbox.setShortCode(content.getShortCode());
        inbox.setMsisdn(content.getMsisdn());
        inbox.setSms(content.getSms());
        inbox.setStatus(AppConstants.N);
        inbox.setCreatedAt(LocalDateTime.now());
        inbox.setUpdatedAt(LocalDateTime.now());

        return inbox;
    }

}
