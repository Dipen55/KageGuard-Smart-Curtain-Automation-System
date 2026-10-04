package com.kageguard.backend.alert;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Component
@ConditionalOnProperty(name = "kageguard.alerts.telegram.enabled", havingValue = "true")
public class TelegramNotifier implements Notifier {

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final String token;
    private final String chatId;

    public TelegramNotifier(@Value("${kageguard.alerts.telegram.token:}") String token,
                            @Value("${kageguard.alerts.telegram.chat-id:}") String chatId) {
        this.token = token;
        this.chatId = chatId;
    }

    @Override
    public String name() {
        return "telegram";
    }

    @Override
    public void send(String subject, String message) throws Exception {
        if (token.isBlank() || chatId.isBlank()) {
            throw new IllegalStateException("Telegram token or chat-id is not set in secrets.properties");
        }

        String text = subject + "\n\n" + message;
        String body = "chat_id=" + URLEncoder.encode(chatId, StandardCharsets.UTF_8)
                + "&text=" + URLEncoder.encode(text, StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder(
                        URI.create("https://api.telegram.org/bot" + token + "/sendMessage"))
                .timeout(Duration.ofSeconds(15))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IllegalStateException(
                    "Telegram answered " + response.statusCode() + ": " + response.body());
        }
    }
}