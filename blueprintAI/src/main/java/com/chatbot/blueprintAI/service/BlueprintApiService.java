package com.chatbot.blueprintAI.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class BlueprintApiService {

    private final WebClient webClient;
    private final String apiKey;
    private final String blueprintUrl;

    public BlueprintApiService(
            @Value("${blueprint.api.key:}") String apiKey,
            @Value("${blueprint.api.url:https://api.blueprint.ai/v1/summarize}") String blueprintUrl
    ) {
        this.apiKey = apiKey;
        this.blueprintUrl = blueprintUrl;
        this.webClient = WebClient.builder()
                .baseUrl(blueprintUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Accept", "application/json")
                .build();
    }

    public String generateClinicalSummary(String prompt) {
        // NOTE: payload format depends on vendor; this is an example JSON shape.
        var payload = Map.of(
                "model", "clinical-summarizer-1",
                "prompt", prompt,
                "max_tokens", 800,
                "temperature", 0.2
        );

        Mono<String> responseMono = webClient.post()
                .uri("") // if blueprintUrl already includes path; otherwise put path here
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class);

        // For simplicity in this example we block; in production prefer reactive handling
        return responseMono.block();
    }
}
