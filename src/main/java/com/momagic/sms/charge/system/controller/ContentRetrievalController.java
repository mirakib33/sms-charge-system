package com.momagic.sms.charge.system.controller;

import com.momagic.sms.charge.system.service.ContentRetrievalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin("*") // Allow cross-origin requests from all origins
@RequestMapping("/api/content")
public class ContentRetrievalController {

    private final ContentRetrievalService contentRetrievalService;

    public ContentRetrievalController(ContentRetrievalService contentRetrievalService) {
        this.contentRetrievalService = contentRetrievalService;
    }

    @GetMapping
    public ResponseEntity<String> fetchContent() {
        contentRetrievalService.fetchAndInsertContent();
        return ResponseEntity.ok("Content fetched and inserted successfully");
    }

}
