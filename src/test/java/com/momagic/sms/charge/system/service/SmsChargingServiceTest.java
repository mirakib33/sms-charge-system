package com.momagic.sms.charge.system.service;

import com.momagic.sms.charge.system.dto.*;
import com.momagic.sms.charge.system.entity.ChargeFailureLog;
import com.momagic.sms.charge.system.entity.ChargeSuccessLog;
import com.momagic.sms.charge.system.entity.Inbox;
import com.momagic.sms.charge.system.repository.*;
import com.momagic.sms.charge.system.utils.constants.AppConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class SmsChargingServiceTest {

    @Mock
    private InboxRepository inboxRepository;

    @Mock
    private KeywordDetailsRepository keywordDetailsRepository;

    @Mock
    private ChargeSuccessLogRepository successLogRepository;

    @Mock
    private ChargeFailureLogRepository failureLogRepository;

    @Mock
    private ChargeConfigRepository chargeConfigRepository;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ExecutorService virtualThreadExecutor;

    @InjectMocks
    private SmsChargingService smsChargingService;

    @Value("${api.unlock.code.url}")
    private String unlockCodeUrl = "http://testUnlockCodeUrl";

    @Value("${api.charge.url}")
    private String chargeUrl = "http://testChargeUrl";

    @Value("${inbox.fetch.limit}")
    private int inboxLimit = 10;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);  // Initialize the mocks
        smsChargingService = new SmsChargingService(
                inboxRepository,
                keywordDetailsRepository,
                successLogRepository,
                failureLogRepository,
                chargeConfigRepository,
                restTemplate,
                virtualThreadExecutor
        );
    }

    @Test
    public void testProcessInboxData() {
        // Setup mocks
        int totalInbox = 5;
        Inbox inbox = createInbox(1); // Helper method to create an Inbox object

        // Mocking the inboxRepository.count() and findAllByStatus
        when(inboxRepository.count()).thenReturn((long) totalInbox);
        when(inboxRepository.findAllByStatus(AppConstants.N, inboxLimit, 0)).thenReturn(Arrays.asList(inbox));

        // Mocking the keywordDetailsRepository.existsById
        when(keywordDetailsRepository.existsById(anyString())).thenReturn(true);

        // Mocking the restTemplate postForEntity call
        when(restTemplate.postForEntity(anyString(), any(), eq(UnlockCodeResponse.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        when(restTemplate.postForEntity(anyString(), any(), eq(ChargeResponse.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        // Call the method under test
        smsChargingService.processInboxData();

        // Verify interactions with the repositories and RestTemplate
        verify(inboxRepository, times(1)).findAllByStatus(AppConstants.N, inboxLimit, 0);
        verify(restTemplate, times(1)).postForEntity(eq(unlockCodeUrl), any(UnlockCodeRequestBody.class), eq(UnlockCodeResponse.class));
        verify(restTemplate, times(1)).postForEntity(eq(chargeUrl), any(ChargeRequestBody.class), eq(ChargeResponse.class));
    }

    private Inbox createInbox(int id) {
        // Helper method to create an Inbox object
        Inbox inbox = new Inbox();
        inbox.setId(id);
        inbox.setTransactionId("transactionId" + id);
        inbox.setOperator("operator" + id);
        inbox.setShortCode("shortCode" + id);
        inbox.setMsisdn("msisdn" + id);
        inbox.setKeyword("keyword" + id);
        inbox.setGameName("gameName" + id);
        inbox.setSms("sms" + id);
        inbox.setStatus(AppConstants.N);
        inbox.setCreatedAt(LocalDateTime.now());
        inbox.setUpdatedAt(LocalDateTime.now());
        return inbox;
    }

    @Test
    public void testProcessSingleInboxData_ValidKeywordAndUnlockCode_Success() {
        // Setup
        Inbox inbox = createInbox(1);

        // Mock the keyword check to return true (valid keyword)
        when(keywordDetailsRepository.existsById(inbox.getKeyword())).thenReturn(true);

        // Mock the unlock code retrieval to return true (success)
        when(restTemplate.postForEntity(eq(unlockCodeUrl), any(UnlockCodeRequestBody.class), eq(UnlockCodeResponse.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        // Mock the charge process to return true (success)
        when(restTemplate.postForEntity(eq(chargeUrl), any(ChargeRequestBody.class), eq(ChargeResponse.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        // Mocking the saving of the inbox status via success log repository
        doNothing().when(successLogRepository).save(any(ChargeSuccessLog.class));

        // Call the method under test
//        smsChargingService.processSingleInboxData(inbox);

        // Verify that the success log was saved
        verify(successLogRepository, times(1)).save(any(ChargeSuccessLog.class));

        // Verify inbox status has been updated to "S" (success)
        verify(inboxRepository, times(1)).save(inbox);
        assertEquals(AppConstants.S, inbox.getStatus());
    }

    @Test
    public void testProcessSingleInboxData_InvalidKeyword_Failure() {
        // Setup
        Inbox inbox = createInbox(1);

        // Mock the keyword check to return false (invalid keyword)
        when(keywordDetailsRepository.existsById(inbox.getKeyword())).thenReturn(false);

        // Call the method under test
//        smsChargingService.processSingleInboxData(inbox);

        // Verify that the inbox status has not been updated
        verify(inboxRepository, times(0)).save(inbox);

        // Ensure no further processing occurs (e.g., charge attempt or success log)
        verify(restTemplate, times(0)).postForEntity(eq(unlockCodeUrl), any(UnlockCodeRequestBody.class), eq(UnlockCodeResponse.class));
        verify(restTemplate, times(0)).postForEntity(eq(chargeUrl), any(ChargeRequestBody.class), eq(ChargeResponse.class));
    }

    @Test
    public void testProcessSingleInboxData_FailedUnlockCode_Failure() {
        // Setup
        Inbox inbox = createInbox(1);

        // Mock the keyword check to return true (valid keyword)
        when(keywordDetailsRepository.existsById(inbox.getKeyword())).thenReturn(true);

        // Mock the unlock code retrieval to return false (failure)
        when(restTemplate.postForEntity(eq(unlockCodeUrl), any(UnlockCodeRequestBody.class), eq(UnlockCodeResponse.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));

        // Call the method under test
//        smsChargingService.processSingleInboxData(inbox);

        // Verify that the inbox status has not been updated
        verify(inboxRepository, times(0)).save(inbox);

        // Ensure no further processing occurs (e.g., charge attempt or success log)
        verify(restTemplate, times(0)).postForEntity(eq(chargeUrl), any(ChargeRequestBody.class), eq(ChargeResponse.class));
    }

    @Test
    public void testProcessSingleInboxData_FailedCharging_Failure() {
        // Setup
        Inbox inbox = createInbox(1);

        // Mock the keyword check to return true (valid keyword)
        when(keywordDetailsRepository.existsById(inbox.getKeyword())).thenReturn(true);

        // Mock the unlock code retrieval to return true (success)
        when(restTemplate.postForEntity(eq(unlockCodeUrl), any(UnlockCodeRequestBody.class), eq(UnlockCodeResponse.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        // Mock the charge process to return failure (non-2xx response)
        when(restTemplate.postForEntity(eq(chargeUrl), any(ChargeRequestBody.class), eq(ChargeResponse.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));

        // Mocking the saving of the inbox status via failure log repository
        doNothing().when(failureLogRepository).save(any(ChargeFailureLog.class));

        // Call the method under test
//        smsChargingService.processSingleInboxData(inbox);

        // Verify that the failure log was saved
        verify(failureLogRepository, times(1)).save(any(ChargeFailureLog.class));

        // Verify inbox status has been updated to "F" (failure)
        verify(inboxRepository, times(1)).save(inbox);
        assertEquals(AppConstants.F, inbox.getStatus());
    }




}