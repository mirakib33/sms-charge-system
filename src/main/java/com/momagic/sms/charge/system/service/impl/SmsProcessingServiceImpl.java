package com.momagic.sms.charge.system.service.impl;

import com.momagic.sms.charge.system.dto.ChargeRequestBody;
import com.momagic.sms.charge.system.dto.ChargeResponse;
import com.momagic.sms.charge.system.dto.UnlockCodeRequestBody;
import com.momagic.sms.charge.system.dto.UnlockCodeResponse;
import com.momagic.sms.charge.system.entity.ChargeFailureLog;
import com.momagic.sms.charge.system.entity.ChargeSuccessLog;
import com.momagic.sms.charge.system.entity.Inbox;
import com.momagic.sms.charge.system.repository.ChargeFailureLogRepository;
import com.momagic.sms.charge.system.repository.ChargeSuccessLogRepository;
import com.momagic.sms.charge.system.repository.InboxRepository;
import com.momagic.sms.charge.system.repository.KeywordDetailsRepository;
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
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
public class SmsProcessingServiceImpl implements SmsProcessingService {

    private final InboxRepository inboxRepository;

    private final KeywordDetailsRepository keywordDetailsRepository;

    private final ChargeSuccessLogRepository successLogRepository;

    private final ChargeFailureLogRepository failureLogRepository;

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
        List<Inbox> inboxMessages = inboxRepository.findAll();

        inboxMessages.forEach(this::processSingleMessage);
    }

    @Async
    private void processSingleMessage(Inbox inbox) {
        try {
            if ("N".equals(inbox.getStatus())) {
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
            ChargeRequestBody requestBody = maptoChargeRequestBody(inbox);

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
            return false;
        }
    }

    private ChargeRequestBody maptoChargeRequestBody(Inbox inbox) {
        return ChargeRequestBody.builder()
                .transactionId(inbox.getTransactionId())
                .operator(inbox.getOperator())
                .shortCode(inbox.getShortCode())
                .msisdn(inbox.getMsisdn())
                .chargeCode(getRandomChargeCode())
                .build();
    }


    public String getRandomChargeCode() {
        String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        int STRING_LENGTH = 8;
        StringBuilder builder = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < STRING_LENGTH;
        i++) {
            int index = random.nextInt(ALPHA_NUMERIC_STRING.length());
            builder.append(ALPHA_NUMERIC_STRING.charAt(index));
        }

        return builder.toString();

    }

    private void logSuccess(Inbox inbox) {
        ChargeSuccessLog log = new ChargeSuccessLog();
        log.setSmsId(inbox.getId());
        log.setTransactionId(inbox.getTransactionId());
        log.setOperator(inbox.getOperator());
        log.setShortCode(inbox.getShortCode());
        log.setMsisdn(inbox.getMsisdn());
        log.setKeyword(inbox.getKeyword());
        log.setGameName(inbox.getGameName());
        log.setCreatedAt(LocalDateTime.now());
        successLogRepository.save(log);
    }

    private void logFailure(Inbox inbox, ChargeResponse response) {
        ChargeFailureLog log = new ChargeFailureLog();
        log.setSmsId(inbox.getId());
        log.setTransactionId(inbox.getTransactionId());
        log.setOperator(inbox.getOperator());
        log.setShortCode(inbox.getShortCode());
        log.setMsisdn(inbox.getMsisdn());
        log.setKeyword(inbox.getKeyword());
        log.setGameName(inbox.getGameName());
        log.setStatusCode(response != null ? response.getStatusCode() : 500);
        log.setMessage(response != null ? response.getMessage() : "Charge response failed. Internal Server Error!");
        log.setCreatedAt(LocalDateTime.now());
        failureLogRepository.save(log);
    }

}
