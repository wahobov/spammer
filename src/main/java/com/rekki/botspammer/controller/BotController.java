package com.rekki.botspammer.controller;

import com.rekki.botspammer.enums.BotState;
import com.rekki.botspammer.model.Groups;
import com.rekki.botspammer.model.User;
import com.rekki.botspammer.service.AsyncSendAPIService;
import com.rekki.botspammer.service.BotService;
import com.rekki.botspammer.service.impl.BotServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class BotController extends TelegramLongPollingBot implements AsyncSendAPIService {

    private final BotService botService;
    static HashMap<String, Groups> groupsHashMap = BotServiceImpl.groups;
    static HashMap<String, User> userHashMap = BotServiceImpl.chatIdUsers;

    @Override
    public String getBotToken() {
        return "5571764306:AAGF3DWyVfIdZ_2sQbZgsIN_yLh1zTZLHok";
    }

    @Override
    public String getBotUsername() {
        return "@hhahahashfasdas_bot";
    }

    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {
        Long groupId = update.getChannelPost().getChatId();
        Long chatId = update.getMessage().getChatId();
        String text = update.getMessage().getText();
        if (update.getMessage().hasText()) {
            if (groupId != null) {
                botService.saveGroups(update);
            } else {
                String chatID = String.valueOf(chatId);
                User user = botService.getUserByChatId(chatID, update);
                switch (user.getBotState())  {
                    case START -> execute(botService.start(update));
                    case GET_MESSAGE -> execute(botService.getMessage(update));
                    case GET_NUMBER -> execute(botService.getNumberOfMessage(update));
                }

                if (text.equals("RESTART \uD83D\uDD04"))
                    execute(botService.start(update));

                if (user.getBotState().equals(BotState.SPAM)) {
                    execute(botService.sendSpam(update));
                    if (user.getBotState().equals(BotState.SPAM_PROCESS))
                        sendToGroups(user.getMessage(), user.getMsgNums());
                }
            }
        }
    }

    @Override
    public void sendText(String text, String chatId) {
        CompletableFuture.runAsync(() -> {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chatId);
            sendMessage.setParseMode(ParseMode.HTML);
            sendMessage.setText(text);
            try {
                if (text.length() > 4096) {
                    sendMessage.setText(text.substring(0, 4096));
                }
                sendApiMethod(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        });
    }
    private void sendToGroups(String text, Long times) {
        for (Groups gro : groupsHashMap.values()) {
            for (int i = 0; i < times; i++) {
                sendText(text, gro.getGroupId());
            }
        }
    }

}
