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
        String[] parts = content.getSms().split(" ");
        String keyword = parts.length >= 1 ? parts[0] : null;
        String gameName = parts.length >= 2 ? parts[1] : null;

        return Inbox.builder()
                .keyword(keyword)
                .gameName(gameName)
                .transactionId(content.getTransactionId())
                .operator(content.getOperator())
                .shortCode(content.getShortCode())
                .msisdn(content.getMsisdn())
                .sms(content.getSms())
                .status(AppConstants.N)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

}
