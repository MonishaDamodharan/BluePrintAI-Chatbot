package com.chatbot.blueprintAI.service;

import org.apache.poi.xwpf.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DocxService {

    // Example pattern to find placeholders like {{chief_complaint}}
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{([^}]+)\\}\\}");

    public Map<String,Object> extractTemplateAndPlaceholders(MultipartFile file) throws Exception {
        Map<String,Object> result = new HashMap<>();
        StringBuilder text = new StringBuilder();
        Set<String> placeholders = new LinkedHashSet<>();

        try (InputStream is = file.getInputStream()) {
            XWPFDocument doc = new XWPFDocument(is);
            for (XWPFParagraph p : doc.getParagraphs()) {
                String para = p.getText();
                if (para != null && !para.isBlank()) {
                    text.append(para).append("\n");
                    Matcher m = PLACEHOLDER_PATTERN.matcher(para);
                    while (m.find()) {
                        placeholders.add(m.group(1).trim());
                    }
                }
            }

            // Also inspect tables (if template uses tables for fields)
            for (XWPFTable table : doc.getTables()) {
                for (XWPFTableRow row : table.getRows()) {
                    for (XWPFTableCell cell : row.getTableCells()) {
                        String cellText = cell.getText();
                        if (cellText != null) {
                            text.append(cellText).append("\n");
                            Matcher m = PLACEHOLDER_PATTERN.matcher(cellText);
                            while (m.find()) placeholders.add(m.group(1).trim());
                        }
                    }
                }
            }
        }

        result.put("text", text.toString());
        result.put("placeholders", new ArrayList<>(placeholders));
        return result;
    }

    /**
     * A simple example that turns a template into a prompt for the AI.
     * You can make this more sophisticated: supply example outputs, specify format (JSON), etc.
     */
    public String buildPromptFromTemplate(String templateText, Object placeholders) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are a clinical summarization assistant. ");
        sb.append("Using the following patient notes template, produce a concise clinical summary following the template sections.\n\n");
        sb.append("TEMPLATE:\n");
        sb.append(templateText).append("\n\n");
        sb.append("INSTRUCTIONS:\n");
        sb.append("1) Fill in each section with clinical content from session notes (synthesize when needed). ");
        sb.append("2) Keep it concise and clinician-friendly. ");
        sb.append("3) If a section is missing, write 'Not documented'.\n\n");
        sb.append("Return the result as plain text or JSON with keys matching the template placeholders.\n");
        return sb.toString();
    }
}
