package com.ecommerce.controller;

import com.ecommerce.ai.AiAssistantService;
import com.ecommerce.util.HttpHelper;
import com.ecommerce.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.util.Map;

/**
 * REST controller for conversational text AI Chatbot interactions.
 */
public class AiAssistantController implements HttpHandler {
    private final AiAssistantService aiAssistantService;

    public AiAssistantController(AiAssistantService aiAssistantService) {
        this.aiAssistantService = aiAssistantService;
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
            String message = (String) req.get("message");
            if (message == null) {
                message = (String) req.get("query");
            }

            AiAssistantService.AssistantResponse resp = aiAssistantService.processQuery(sessionId, message, false);
            HttpHelper.sendJsonResponse(exchange, 200, resp);
        } catch (Exception e) {
            HttpHelper.sendErrorResponse(exchange, 500, "AI Chatbot Error: " + e.getMessage());
        }
    }
}
