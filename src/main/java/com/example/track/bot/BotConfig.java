package com.example.track.bot;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
@RequiredArgsConstructor
public class BotConfig {
    private final BotCode botCode;

    @PostConstruct
    public void registerBot() {
        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(botCode);
            System.out.println("✅ Bot muvaffaqiyatli ro‘yxatdan o‘tdi.");
        } catch (TelegramApiException e) {
            System.err.println("❌ Bot ro‘yxatdan o‘tishda xatolik: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
