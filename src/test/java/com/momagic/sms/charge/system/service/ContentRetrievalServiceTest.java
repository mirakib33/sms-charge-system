package com.momagic.sms.charge.system.service;

import com.momagic.sms.charge.system.dto.Content;
import com.momagic.sms.charge.system.dto.ContentResponse;
import com.momagic.sms.charge.system.entity.Inbox;
import com.momagic.sms.charge.system.repository.InboxRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ContentRetrievalServiceTest {

    @Mock
    private InboxRepository inboxRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ContentRetrievalService contentRetrievalService;

    @Value("${api.content.url}")
    private String contentUrl = "http://mock-api.com/content";

    private ExecutorService virtualThreadExecutor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();
        ReflectionTestUtils.setField(contentRetrievalService, "virtualThreadExecutor", virtualThreadExecutor);
        ReflectionTestUtils.setField(contentRetrievalService, "contentUrl", contentUrl);
    }

    @Test
    void testFetchAndInsertContent_Success() {
        Content content1 = new Content();
        content1.setTransactionId("c2fe9ad4-e290-4592-a36b-7acf80e9452d");
        content1.setOperator("ROBI");
        content1.setShortCode("16957");
        content1.setMsisdn("8801872171602");
        content1.setSms("AMAZON ALADDIN 00000000000000019401 23815 K33");

        ContentResponse contentResponse = new ContentResponse();
        contentResponse.setStatusCode(200);
        contentResponse.setMessage("Success");
        contentResponse.setContentCount(100);
        contentResponse.setContents(Collections.singletonList(content1));

        when(restTemplate.getForEntity(contentUrl, ContentResponse.class))
                .thenReturn(new ResponseEntity<>(contentResponse, HttpStatus.OK));

        contentRetrievalService.fetchAndInsertContent();

        verify(inboxRepository, times(1)).save(any(Inbox.class));
    }

    @Test
    void testFetchAndInsertContent_EmptyResponse() {
        ContentResponse contentResponse = new ContentResponse();
        contentResponse.setStatusCode(200);
        contentResponse.setMessage("Success");
        contentResponse.setContentCount(0);
        contentResponse.setContents(Collections.emptyList());

        when(restTemplate.getForEntity(contentUrl, ContentResponse.class))
                .thenReturn(new ResponseEntity<>(contentResponse, HttpStatus.OK));

        contentRetrievalService.fetchAndInsertContent();

        verify(inboxRepository, never()).save(any(Inbox.class));
    }

    @Test
    void testFetchAndInsertContent_FailureResponse() {
        when(restTemplate.getForEntity(contentUrl, ContentResponse.class))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR));

        contentRetrievalService.fetchAndInsertContent();

        verify(inboxRepository, never()).save(any(Inbox.class));
    }

    @Test
    void testMapToInbox_ValidContent() throws Exception {
        Method mapToInboxMethod = ContentRetrievalService.class.getDeclaredMethod("mapToInbox", Content.class);
        mapToInboxMethod.setAccessible(true);

        Content content = new Content();
        content.setTransactionId("c2fe9ad4-e290-4592-a36b-7acf80e9452d");
        content.setOperator("ROBI");
        content.setShortCode("16957");
        content.setMsisdn("8801872171602");
        content.setSms("AMAZON ALADDIN 00000000000000019401 23815 K33");

        Inbox inbox = (Inbox) mapToInboxMethod.invoke(contentRetrievalService, content);

        assertNotNull(inbox);
        assertEquals("AMAZON", inbox.getKeyword());
        assertEquals("ALADDIN", inbox.getGameName());
        assertEquals("c2fe9ad4-e290-4592-a36b-7acf80e9452d", inbox.getTransactionId());
        assertEquals("ROBI", inbox.getOperator());
        assertEquals("16957", inbox.getShortCode());
        assertEquals("8801872171602", inbox.getMsisdn());
        assertEquals("AMAZON ALADDIN 00000000000000019401 23815 K33", inbox.getSms());
        assertEquals("N", inbox.getStatus());
        assertNotNull(inbox.getCreatedAt());
        assertNotNull(inbox.getUpdatedAt());
    }

    @Test
    void testMapToInbox_InvalidContent() throws Exception {
        Method mapToInboxMethod = ContentRetrievalService.class.getDeclaredMethod("mapToInbox", Content.class);
        mapToInboxMethod.setAccessible(true);

        Content content = new Content();
        content.setTransactionId("c2fe9ad4-e290-4592-a36b-7acf80e9452d");
        content.setOperator("ROBI");
        content.setShortCode("16957");
        content.setMsisdn("8801872171602");
        content.setSms("AMAZON ALADDIN 00000000000000019401 23815 K33");

        Inbox inbox = (Inbox) mapToInboxMethod.invoke(contentRetrievalService, content);

        assertNotNull(inbox);
        assertEquals("AMAZON", inbox.getKeyword());
        assertEquals("ALADDIN", inbox.getGameName());
        assertEquals("c2fe9ad4-e290-4592-a36b-7acf80e9452d", inbox.getTransactionId());
        assertEquals("ROBI", inbox.getOperator());
        assertEquals("16957", inbox.getShortCode());
        assertEquals("8801872171602", inbox.getMsisdn());
        assertEquals("AMAZON ALADDIN 00000000000000019401 23815 K33", inbox.getSms());
        assertEquals("N", inbox.getStatus());
        assertNotNull(inbox.getCreatedAt());
        assertNotNull(inbox.getUpdatedAt());
    }
}
