package org.example.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.example.dto.GptRequest;
import org.example.dto.GptResponse;
import org.example.dto.Transcription;

import java.io.File;
import java.util.List;

@Component
//@RequiredArgsConstructor
public class OpenAiClient {

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.chat.url}")
    private String chatApiUrl;

    @Value("${openai.api.chat.model}")
    private String chatModel;

    @Value("${openai.api.chat.system_role}")
    private String systemRole;

    @Value("${openai.api.transcription.url}")
    private String transcriptionApiUrl;

    @Value("${openai.api.transcription.model}")
    private String voiceModel;

    @Value("${openai.api.transcription.language}")
    private String language;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // Constructor injection
    @Autowired
    public OpenAiClient(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public String promptModel(String prompt) {
        try {
            // Добавлено логирование запроса
            System.out.println("[DEBUG] Sending prompt to GPT: " + prompt.substring(0, Math.min(prompt.length(), 100)) + "...");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            GptRequest body = GptRequest.builder()
                    .model(chatModel)
                    .messages(List.of(
                            GptRequest.Message.builder()
                                    .role("system")
                                    .content(systemRole)
                                    .build(),
                            GptRequest.Message.builder()
                                    .role("user")
                                    .content(prompt)
                                    .build()
                    ))
                    .build();

            HttpEntity<GptRequest> request = new HttpEntity<>(body, headers);

            // логирование запроса
            System.out.println("[DEBUG] Sending request to GPT API with model: " + chatModel);

            ResponseEntity<String> response = restTemplate.postForEntity(chatApiUrl, request, String.class);

            // Логируем сырой ответ для отладки
            System.out.println("[DEBUG] Raw GPT response: " + response.getBody());

            // проверка на пустой ответ
            if (response.getBody() == null || response.getBody().isEmpty()) {
                throw new IllegalStateException("Empty response from GPT API");
            }

            GptResponse responseBody = objectMapper.readValue(response.getBody(), GptResponse.class);

            return responseBody.getFirstAnswer();

        } catch (HttpClientErrorException e) {
            // обработка HTTP ошибок
            // Логируем полную ошибку API
            System.err.println("GPT API Error: " + e.getResponseBodyAsString());
            throw new IllegalStateException("GPT API request failed: " + e.getStatusCode() + " - " + e.getResponseBodyAsString(), e);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to parse GPT response", e);
        } catch (Exception e) {
            throw new IllegalStateException("Unexpected error in promptModel: " + e.getMessage(), e);
        }
    }

    public String transcribe(File audio) {
        try {
            // проверка файла
            if (!audio.exists() || audio.length() == 0) {
                throw new IllegalArgumentException("Audio file is empty or doesn't exist");
            }

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + apiKey);
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new FileSystemResource(audio));
            body.add("model", voiceModel);

            // Язык теперь добавляется только если указан
            if (language != null && !language.isEmpty()) {
                body.add("language", language);
            }

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // Добавлено логирование
            System.out.println("[DEBUG] Sending audio for transcription, size: " + audio.length() + " bytes");

            ResponseEntity<String> response = restTemplate.postForEntity(
                    transcriptionApiUrl,
                    requestEntity,
                    String.class
            );

            // Добавлена проверка на пустой ответ
            if (response.getBody() == null) {
                throw new IllegalStateException("Empty transcription response");
            }

            Transcription transcription = objectMapper.readValue(response.getBody(), Transcription.class);

            // Добавлена проверка результата
            if (transcription.text() == null || transcription.text().isEmpty()) {
                throw new IllegalStateException("Received empty transcription");
            }

            return transcription.text();

        } catch (HttpClientErrorException e) {
            throw new IllegalStateException("Transcription API error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString(), e);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to parse transcription response", e);
        } catch (Exception e) {
            throw new IllegalStateException("Unexpected error in transcribe: " + e.getMessage(), e);
        }
    }
}

//    public String promptModel(String prompt) {
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        headers.setBearerAuth(apiKey);
//
//        GptRequest body = GptRequest.builder()
//                .model(chatModel)
//                .messages(List.of(
//                        GptRequest.Message.builder()
//                                .role("system")
//                                .content(systemRole)
//                                .build(),
//                        GptRequest.Message.builder()
//                                .role("user")
//                                .content(prompt)
//                                .build()
//                ))
//                .build();
//
//        HttpEntity<GptRequest> request = new HttpEntity<>(body, headers);
//        ResponseEntity<String> response = restTemplate.postForEntity(chatApiUrl, request, String.class);
//        GptResponse responseBody;
//        try {
//            responseBody = objectMapper.readValue(response.getBody(), GptResponse.class);
//        } catch (JsonProcessingException e) {
//            throw new IllegalStateException("There's an error when parsing JSON response from GPT", e);
//        }
//        return responseBody.getChoices().get(0).getMessage().getContent();
//    }
//
//    public String transcribe(File audio) {
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("Authorization", "Bearer " + apiKey);
//        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
//
//        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
//        FileSystemResource fileResource = new FileSystemResource(audio);
//        body.add("file", fileResource);
//        body.add("model", voiceModel);
//        body.add("language", language);
//
//        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
//        ResponseEntity<String> response = restTemplate.postForEntity(transcriptionApiUrl, requestEntity, String.class);
//        Transcription transcription;
//        try {
//            transcription = objectMapper.readValue(response.getBody(), Transcription.class);
//        } catch (JsonProcessingException e) {
//            throw new IllegalStateException("There was an error when converting JSON response to DTO", e);
//        }
//        return transcription.text();
//    }
//}