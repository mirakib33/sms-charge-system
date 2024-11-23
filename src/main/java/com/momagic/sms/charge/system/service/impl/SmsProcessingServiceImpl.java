package com.momagic.sms.charge.system.service.impl;

import com.momagic.sms.charge.system.dto.ChargeRequestBody;
import com.momagic.sms.charge.system.dto.ChargeResponse;
import com.momagic.sms.charge.system.dto.UnlockCodeRequestBody;
import com.momagic.sms.charge.system.dto.UnlockCodeResponse;
import com.momagic.sms.charge.system.entity.ChargeFailureLog;
import com.momagic.sms.charge.system.entity.ChargeSuccessLog;
import com.momagic.sms.charge.system.entity.Inbox;
import com.momagic.sms.charge.system.repository.*;
import com.momagic.sms.charge.system.service.SmsProcessingService;
import com.momagic.sms.charge.system.utils.AppConstants;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SmsProcessingServiceImpl implements SmsProcessingService {

    private final InboxRepository inboxRepository;
    private final KeywordDetailsRepository keywordDetailsRepository;
    private final ChargeSuccessLogRepository successLogRepository;
    private final ChargeFailureLogRepository failureLogRepository;
    private final ChargeConfigRepository chargeConfigRepository;
    private final RestTemplate restTemplate;
//    private final Executor virtualThreadExecutor;

    @Value("${api.unlock.code.url}")
    private String unlockCodeUrl;

    @Value("${api.charge.url}")
    private String chargeUrl;


    @PostConstruct
    public void init() {
        System.out.println("Processing sms...");
        processInboxMessages();
    }

    @Override
    public void processInboxMessages() {
        List<Inbox> inboxMessages = inboxRepository.findAllByStatus(AppConstants.N);

        inboxMessages.forEach(this::processSingleMessage);
    }

    @Async
    private void processSingleMessage(Inbox inbox) {
        try {
            // Validate keyword dynamically
            boolean isValidKeyword = keywordDetailsRepository.existsById(inbox.getKeyword());
            if (!isValidKeyword) {
                inbox.setStatus(AppConstants.F);
                inbox.setUpdatedAt(LocalDateTime.now());
                inboxRepository.save(inbox);
                return;
            }

            // Retrieve unlock code
            UnlockCodeResponse unlockCode = getUnlockCode(inbox);
            if (unlockCode == null) {
                inbox.setStatus(AppConstants.F);
                inbox.setUpdatedAt(LocalDateTime.now());
                inboxRepository.save(inbox);
                return;
            }

            // Perform charging
            boolean chargeSuccess = performCharging(inbox);
            if (chargeSuccess) {
                inbox.setStatus(AppConstants.S);
                logSuccess(inbox);
            } else {
                inbox.setStatus(AppConstants.F);
                inbox.setUpdatedAt(LocalDateTime.now());
                inboxRepository.save(inbox);
            }
        } catch (Exception e) {
            inbox.setStatus("F");
            inbox.setUpdatedAt(LocalDateTime.now());
            inboxRepository.save(inbox);
        }
    }

    private UnlockCodeResponse getUnlockCode(Inbox inbox) {
        try {
            UnlockCodeRequestBody requestBody = mapToUnlockCodeRequestBody(inbox);

            ResponseEntity<UnlockCodeResponse> response = restTemplate.postForEntity(
                    unlockCodeUrl, requestBody, UnlockCodeResponse.class
            );

            if (response != null && response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private UnlockCodeRequestBody mapToUnlockCodeRequestBody(Inbox inbox) {
        return UnlockCodeRequestBody.builder()
                .transactionId(inbox.getTransactionId())
                .operator(inbox.getOperator())
                .shortCode(inbox.getShortCode())
                .msisdn(inbox.getMsisdn())
                .keyword(inbox.getKeyword())
                .gameName(inbox.getGameName())
                .build();
    }

    private boolean performCharging(Inbox inbox) {
        try {
            String chargeCode = chargeConfigRepository.findChargeCodeByOperator(inbox.getOperator())
                    .orElseThrow(() -> new RuntimeException("Charge code not found for operator: " + inbox.getOperator()));

            ChargeRequestBody requestBody = maptoChargeRequestBody(inbox, chargeCode);

            ResponseEntity<ChargeResponse> response = restTemplate.postForEntity(
                    chargeUrl, requestBody, ChargeResponse.class
            );
            if (response != null && response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return true;
            }
            logFailure(inbox, response.getBody());
            return false;
        } catch (Exception e) {
            logFailure(inbox, null);
            System.out.println(e);
            return false;
        }
    }

    private ChargeRequestBody maptoChargeRequestBody(Inbox inbox, String chargeCode) {
        return ChargeRequestBody.builder()
                .transactionId(inbox.getTransactionId())
                .operator(inbox.getOperator())
                .shortCode(inbox.getShortCode())
                .msisdn(inbox.getMsisdn())
                .chargeCode(chargeCode)
                .build();
    }

    private void logSuccess(Inbox inbox) {
        ChargeSuccessLog log = ChargeSuccessLog.builder()
                .smsId(inbox.getId())
                .transactionId(inbox.getTransactionId())
                .operator(inbox.getOperator())
                .shortCode(inbox.getShortCode())
                .msisdn(inbox.getMsisdn())
                .keyword(inbox.getKeyword())
                .gameName(inbox.getGameName())
                .createdAt(LocalDateTime.now())
                .build();

        successLogRepository.save(log);
    }

    private void logFailure(Inbox inbox, ChargeResponse response) {
        ChargeFailureLog log = ChargeFailureLog.builder()
                .smsId(inbox.getId())
                .transactionId(inbox.getTransactionId())
                .operator(inbox.getOperator())
                .shortCode(inbox.getShortCode())
                .msisdn(inbox.getMsisdn())
                .keyword(inbox.getKeyword())
                .gameName(inbox.getGameName())
                .statusCode(response != null ? response.getStatusCode() : 500)
                .message(response != null ? response.getMessage() : "Charge response failed. Internal Server Error!")
                .createdAt(LocalDateTime.now())
                .build();

        failureLogRepository.save(log);
    }

}
