package com.example.track.bot;

import com.example.track.entity.Role;
import com.example.track.service.LocationService;
import com.example.track.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendLocation;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageLiveLocation;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class BotCode extends TelegramLongPollingBot {

    @Value("${bot.username}")
    private String username;

    @Value("${bot.token}")
    private String token;

    @Value("${guide.text}")
    private String guide;

    private final UserService userService;
    private final LocationService locationService;

    private Integer liveMessageId;

    @Override
    public String getBotUsername() {
        return username;
    }

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage()) return;

        Message msg = update.getMessage();
        Long chatId = msg.getChatId();
        String username = msg.getChat().getUserName();

        if (msg.hasText()) handleTextMessage(chatId, msg.getText());
        if (msg.hasLocation()) handleLocation(chatId, msg.getLocation().getLatitude(), msg.getLocation().getLongitude());
    }

    private void handleTextMessage(Long chatId, String text) {
        Long adminChatId = 7193645528L;

        if (text.startsWith("/start")) {
            if (Objects.equals(chatId, adminChatId)) {
                sendAdminMenu(chatId);
                return;
            }
            if (text.contains("track_")) {
                Long parentChatId = Long.parseLong(text.replace("/start track_", ""));
                userService.addChild(parentChatId,chatId);
                sendMessage(chatId, "Kuzatuv rejimi yoqildi ✅");
                sendMessage(parentChatId, "Aloqa o'rnatildi: " + username + " 📍");
                sendShareLocationButton(chatId);
            } else {
                sendMenu(chatId, userService.saveParent(chatId));
            }
        } else if (text.equals("➕ Kuzatuv qo‘shish")) {
            String link = userService.generateLink(chatId);
            sendMessage(chatId, "Kuzatish uchun ✅\nUshbu linkni yuboring:\n" + link);
        } else if (text.equals("📊 Statistikalar")) {
            sendStatistics(chatId);
        } else if (text.equals("📖 Qo‘llanma")) {
            sendMessage(chatId, guide);
        }
    }

    private void handleLocation(Long chatId, double lat, double lon) {
        Long parentChatId = userService.findParent(chatId);
        if (parentChatId != null) {
            locationService.saveLocation(chatId);
            sendOrUpdateLiveLocation(parentChatId, lat, lon);
        }
    }

    private void sendMenu(Long chatId, String text) {
        SendMessage message = new SendMessage(chatId.toString(), text);
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        keyboard.setResizeKeyboard(true);
        KeyboardRow row1 = new KeyboardRow();
        row1.add("➕ Kuzatuv qo‘shish");
        row1.add("📖 Qo‘llanma");
        keyboard.setKeyboard(List.of(row1));
        message.setReplyMarkup(keyboard);
        executeSafely(message);
    }

    private void sendShareLocationButton(Long chatId) {
        SendMessage message = new SendMessage(chatId.toString(), "Malumotlarni ulashing:");
        KeyboardButton locationButton = new KeyboardButton("📍 Ulashish");
        locationButton.setRequestLocation(true);
        KeyboardRow row = new KeyboardRow();
        row.add(locationButton);
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        keyboard.setResizeKeyboard(true);
        keyboard.setOneTimeKeyboard(true);
        keyboard.setKeyboard(List.of(row));
        message.setReplyMarkup(keyboard);
        executeSafely(message);
    }

    private void sendOrUpdateLiveLocation(Long parentChatId, double lat, double lon) {
        try {
            if (liveMessageId == null) {
                SendLocation locationMessage = new SendLocation();
                locationMessage.setChatId(parentChatId.toString());
                locationMessage.setLatitude(lat);
                locationMessage.setLongitude(lon);
                locationMessage.setLivePeriod(60 * 60 * 8);
                liveMessageId = execute(locationMessage).getMessageId();
            } else {
                EditMessageLiveLocation edit = new EditMessageLiveLocation();
                edit.setChatId(parentChatId.toString());
                edit.setMessageId(liveMessageId);
                edit.setLatitude(lat);
                edit.setLongitude(lon);
                execute(edit);
            }
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendAdminMenu(Long chatId) {
        SendMessage message = new SendMessage(chatId.toString(), "Admin panelga xush kelibsiz. Menuni tanlang:");
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        keyboard.setResizeKeyboard(true);
        KeyboardRow row = new KeyboardRow();
        row.add("📊 Statistikalar");
        keyboard.setKeyboard(List.of(row));
        message.setReplyMarkup(keyboard);
        executeSafely(message);
    }

    private void sendStatistics(Long chatId) {
        long totalUsers = userService.countAllUsers();
        long totalParents = userService.countByRole(Role.PARENT);
        long totalChildren = userService.countByRole(Role.CHILD);

        String stats = "📈 Statistikalar:\n\n" +
                "Jami foydalanuvchilar: " + totalUsers + "\n" +
                "Kuzatuvchilar: " + totalParents + "\n" +
                "Kuzatuvdagilar: " + totalChildren;
        sendMessage(chatId, stats);
    }

    public void sendMessage(Long chatId, String text) {
        executeSafely(new SendMessage(chatId.toString(), text));
    }

    private void executeSafely(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
