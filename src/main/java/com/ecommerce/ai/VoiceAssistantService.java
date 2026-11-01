package com.ecommerce.ai;

import java.util.Locale;

/**
 * Dedicated Voice Assistant service for voice command parsing,
 * speech normalization, and acoustic TTS response formatting.
 */
public class VoiceAssistantService {
    private final AiAssistantService aiAssistantService;

    public VoiceAssistantService(AiAssistantService aiAssistantService) {
        this.aiAssistantService = aiAssistantService;
    }

    public static class VoiceResponse {
        private final String recognizedText;
        private final String spokenResponse;
        private final String displayText;
        private final String action;
        private final Object data;
        private final double speechRate;
        private final double speechPitch;

        public VoiceResponse(String recognizedText, String spokenResponse, String displayText, 
                             String action, Object data, double speechRate, double speechPitch) {
            this.recognizedText = recognizedText;
            this.spokenResponse = spokenResponse;
            this.displayText = displayText;
            this.action = action;
            this.data = data;
            this.speechRate = speechRate;
            this.speechPitch = speechPitch;
        }

        public String getRecognizedText() {
            return recognizedText;
        }

        public String getSpokenResponse() {
            return spokenResponse;
        }

        public String getDisplayText() {
            return displayText;
        }

        public String getAction() {
            return action;
        }

        public Object getData() {
            return data;
        }

        public double getSpeechRate() {
            return speechRate;
        }

        public double getSpeechPitch() {
            return speechPitch;
        }
    }

    public VoiceResponse processVoiceCommand(String sessionId, String speechTranscript) {
        if (speechTranscript == null || speechTranscript.trim().isEmpty()) {
            return new VoiceResponse(
                "",
                "I didn't catch that. Please speak again.",
                "Listening...",
                "NONE",
                null,
                1.0,
                1.0
            );
        }

        String normalized = normalizeVoiceTranscript(speechTranscript);
        AiAssistantService.AssistantResponse aiResp = aiAssistantService.processQuery(sessionId, normalized, true);

        return new VoiceResponse(
            speechTranscript,
            aiResp.getSpokenText(),
            aiResp.getMessage(),
            aiResp.getAction(),
            aiResp.getData(),
            1.05, // slightly lively natural speech pace
            1.0
        );
    }

    private String normalizeVoiceTranscript(String text) {
        String clean = text.toLowerCase(Locale.ROOT).trim();
        // Strip wake-words or introductory politeness phrases and punctuation
        clean = clean.replaceAll("(?i)^(hey\\s+assistant|ok\\s+assistant|hello\\s+assistant|assistant|alexa|siri|computer)[,\\s]*", "");
        clean = clean.replaceAll("(?i)^(could\\s+you\\s+please|can\\s+you\\s+please|please)[,\\s]*", "");
        clean = clean.replaceAll("(?i)[,\\s]*(please)[,\\s]*$", "");
        clean = clean.replaceAll("^[\\s,.]+", "").trim();
        return clean;
    }
}
