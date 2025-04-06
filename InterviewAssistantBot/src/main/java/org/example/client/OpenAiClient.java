package org.example.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class OpenAiClient {

    @Value("")
    private String apiKey;

    @Value("")
    private String chatApiUrl;

    @Value("")
    private String chatModel;

    @Value("")
    private String systemRole;

    @Value("")
    private String transcriptionApiUrl;

    @Value("")
    private String voiceModel;

    @Value("")
    private String language;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public String promptModel(String prompt){

    }

    public String transcribe(){

    }

}
