package com.momagic.sms.charge.system.service;

import com.momagic.sms.charge.system.dto.Content;
import com.momagic.sms.charge.system.dto.ContentResponse;
import com.momagic.sms.charge.system.entity.Inbox;
import com.momagic.sms.charge.system.repository.InboxRepository;
import com.momagic.sms.charge.system.utils.constants.AppConstants;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;

@Slf4j
@RequiredArgsConstructor
@Service("contentRetrievalService")
public class ContentRetrievalService {

    private final InboxRepository inboxRepository;
    private final RestTemplate restTemplate;
    private final ExecutorService virtualThreadExecutor;

    @Value("${api.content.url}")     // Injects content API URL from application properties
    private String contentUrl;

    /**
     * Initializes the service by scheduling the initial content fetch.
     * Runs after bean creation (or starting time of the application)
     */
    @PostConstruct
    public void init() {
        virtualThreadExecutor.submit(this::fetchAndInsertContent);
    }

    /**
     * Schedules a task to fetch and insert content every 6 hours.
     */
    @Scheduled(fixedRate = 6 * 60 * 60 * 1000) // Every 6 hours
    public void scheduledTask() {
        virtualThreadExecutor.submit(this::fetchAndInsertContent);
    }

    /**
     * Fetches content from the content provider service and inserts it into the database.
     */
    public void fetchAndInsertContent() {
        try {
            ResponseEntity<ContentResponse> response = restTemplate.getForEntity(contentUrl, ContentResponse.class);

            if (response != null && response.getStatusCode() == HttpStatus.OK) {
                ContentResponse contentResponse = response.getBody();
                if (contentResponse != null && contentResponse.getStatusCode() == 200 && !contentResponse.getContents().isEmpty()) {
                    for (Content content : contentResponse.getContents()) {
                        inboxRepository.save(mapToInbox(content));
                    }
                } else {
                    log.info("Empty content response or unsuccessful status code: {}", contentResponse);
                }
            } else {
                log.warn("Failed to fetch content: {}", response.getStatusCodeValue());
            }
        } catch (Exception e) {
            log.error("Error fetching content", e);
        }
    }

    /**
     * Maps a `Content` object to an `Inbox` object for database storage.
     *
     * @param content The content to be mapped
     * @return The mapped Inbox object
     */
    private Inbox mapToInbox(Content content) {
        String[] parts = content.getSms().split(" "); // Splits SMS content into parts (keyword and gameName)
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
