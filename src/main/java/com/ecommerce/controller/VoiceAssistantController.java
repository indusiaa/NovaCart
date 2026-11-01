package com.ecommerce.controller;

import com.ecommerce.ai.VoiceAssistantService;
import com.ecommerce.util.HttpHelper;
import com.ecommerce.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.util.Map;

/**
 * REST controller for hands-free voice assistant interactions, speech command parsing,
 * and audio text-to-speech synchronization.
 */
public class VoiceAssistantController implements HttpHandler {
    private final VoiceAssistantService voiceAssistantService;

    public VoiceAssistantController(VoiceAssistantService voiceAssistantService) {
        this.voiceAssistantService = voiceAssistantService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (HttpHelper.handlePreflight(exchange)) return;

        String method = exchange.getRequestMethod().toUpperCase();
        if (!"POST".equals(method)) {
            HttpHelper.sendErrorResponse(exchange, 405, "Method Not Allowed. Use POST.");
            return;
        }

        String sessionId = HttpHelper.getSessionId(exchange);

        try {
            String body = HttpHelper.readRequestBody(exchange);
            Map<String, Object> req = JsonUtil.parseObject(body);
            String transcript = (String) req.get("transcript");
            if (transcript == null) {
                transcript = (String) req.get("speechTranscript");
            }
            if (transcript == null) {
                transcript = (String) req.get("text");
            }

            VoiceAssistantService.VoiceResponse resp = voiceAssistantService.processVoiceCommand(sessionId, transcript);
            HttpHelper.sendJsonResponse(exchange, 200, resp);
        } catch (Exception e) {
            HttpHelper.sendErrorResponse(exchange, 500, "Voice Assistant Error: " + e.getMessage());
        }
    }
}
