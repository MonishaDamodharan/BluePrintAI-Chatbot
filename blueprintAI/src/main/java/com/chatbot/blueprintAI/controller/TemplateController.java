package com.chatbot.blueprintAI.controller;

import com.chatbot.blueprintAI.service.BlueprintApiService;
import com.chatbot.blueprintAI.service.DocxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class TemplateController {

    @Autowired
    private DocxService docxService;

    @Autowired
    private BlueprintApiService blueprintApiService;

    @PostMapping(value = "/upload-template", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadTemplate(@RequestPart("file") MultipartFile file) {
        try {
            // 1) Extract plain text and placeholder fields from docx
            Map<String, Object> extracted = docxService.extractTemplateAndPlaceholders(file);

            String templateText = (String) extracted.get("text");
            // placeholders can be something like List<String> or Map<String,String>
            Object placeholders = extracted.get("placeholders");

            // 2) Build prompt / payload for Blueprint
            String prompt = docxService.buildPromptFromTemplate(templateText, placeholders);

            // 3) Call Blueprint API to generate clinical summary
            String aiSummary = blueprintApiService.generateClinicalSummary(prompt);

            // 4) Return the summary (or save to DB/EHR)
            return ResponseEntity.ok(Map.of("summary", aiSummary));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}
