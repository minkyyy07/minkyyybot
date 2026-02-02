package com.example.demo.bot;

import com.example.demo.service.LocalStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.InputStream;
import java.net.URL;
import java.util.List;

@Component
public class TelegramFileBot extends TelegramLongPollingBot {

    private final LocalStorageService storageService;

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Value("${telegram.bot.token}")
    private String botToken;

    public TelegramFileBot(LocalStorageService storageService) {
        this.storageService = storageService;
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage()) {
                Message msg = update.getMessage();

                if (msg.hasText() && "/start".equals(msg.getText())) {
                    sendStartMenu(msg.getChatId());
                    return;
                }

                String fileId = extractFileId(msg);
                if (fileId != null) {
                    String fileUrl = getFileUrl(fileId);
                    byte[] data = download(fileUrl);

                    String fileName = System.currentTimeMillis() + "_" + fileId;
                    String savedPath = storageService.save(data, fileName);

                    sendMessage(msg.getChatId(), "✅ Файл сохранён локально:\n" + savedPath);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendStartMenu(Long chatId) throws TelegramApiException {
        InlineKeyboardButton btn = new InlineKeyboardButton("📤 Отправить файл");
        btn.setCallbackData("send_file");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(List.of(List.of(btn)));

        SendMessage message = new SendMessage(chatId.toString(), "Привет! Отправь любой файл.");
        message.setReplyMarkup(markup);
        execute(message);
    }

    private String extractFileId(Message msg) {
        if (msg.hasDocument()) return msg.getDocument().getFileId();
        if (msg.hasPhoto()) return msg.getPhoto().get(msg.getPhoto().size() - 1).getFileId();
        if (msg.hasVideo()) return msg.getVideo().getFileId();
        if (msg.hasAudio()) return msg.getAudio().getFileId();
        return null;
    }

    private String getFileUrl(String fileId) throws TelegramApiException {
        GetFile getFile = new GetFile(fileId);
        org.telegram.telegrambots.meta.api.objects.File file = execute(getFile);
        return file.getFileUrl(getBotToken());
    }

    private byte[] download(String fileUrl) throws Exception {
        try (InputStream in = new URL(fileUrl).openStream()) {
            return in.readAllBytes();
        }
    }

    private void sendMessage(Long chatId, String text) throws TelegramApiException {
        SendMessage message = new SendMessage(chatId.toString(), text);
        execute(message);
    }

    @Override
    public String getBotUsername() { return botUsername; }

    @Override
    public String getBotToken() { return botToken; }
}