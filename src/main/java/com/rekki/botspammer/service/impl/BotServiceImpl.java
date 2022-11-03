package com.rekki.botspammer.service.impl;

import com.rekki.botspammer.enums.BotState;
import com.rekki.botspammer.model.Groups;
import com.rekki.botspammer.model.User;
import com.rekki.botspammer.service.AsyncSendAPIService;
import com.rekki.botspammer.service.BotService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service
public class BotServiceImpl implements BotService {

    public static HashMap<String, User> chatIdUsers = new HashMap<>();
    public static HashMap<String, Groups> groups = new HashMap<>();

    @Override
    public SendMessage start(Update update) {
        String chatId = chatId(update);
        User user = getUserByChatId(chatId, update);
        user.setBotState(BotState.GET_MESSAGE);
        chatIdUsers.put(chatId, user);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText("Please enter the message to spam.\nIf you want to cancel please click the button /cancel");
        return sendMessage;
    }

    @Override
    public SendMessage getMessage(Update update) {
        String chatId = chatId(update);
        String text = update.getMessage().getText();
        User user = getUserByChatId(chatId, update);
        user.setBotState(BotState.GET_NUMBER);
        user.setMessage(text);
        chatIdUsers.put(chatId, user);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText("Please enter the number of message that should ne sent.\nIf you want to cancel please click the button /cancel");
        return sendMessage;
    }

    @Override
    public SendMessage getNumberOfMessage(Update update) {
        String chatId = chatId(update);
        try {
            Long nums = Long.parseLong(update.getMessage().getText());
            User user = getUserByChatId(chatId, update);
            user.setBotState(BotState.SPAM);
            user.setMsgNums(nums);
            chatIdUsers.put(chatId, user);
            ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
            replyKeyboardMarkup.setResizeKeyboard(true);
            replyKeyboardMarkup.setSelective(true);

            List<KeyboardRow> rows = new ArrayList<>();
            KeyboardRow row = new KeyboardRow();
            KeyboardButton button = new KeyboardButton();
            button.setText("YES");
            row.add(button);
            rows.add(row);

            row = new KeyboardRow();
            button = new KeyboardButton();
            button.setText("CANCEL");
            row.add(button);
            rows.add(row);

            replyKeyboardMarkup.setKeyboard(rows);

            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chatId);
            sendMessage.setText("Do you really want to send this SPAM message?\nIf you want to cancel please click the button /cancel");
            sendMessage.setReplyMarkup(replyKeyboardMarkup);
            return sendMessage;
        } catch (Exception e) {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setText("WRONG THE NUMBER. Please enter in the number");
            sendMessage.setChatId(chatId);
            return sendMessage;
        }

    }

    @Override
    public SendMessage sendSpam(Update update) {
        String answer = update.getMessage().getText();
        String chatId = chatId(update);
        User user = getUserByChatId(chatId, update);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText("Choose the button below");

        if (answer.equals("YES")) {
            user.setBotState(BotState.SPAM_PROCESS);
            chatIdUsers.put(chatId, user);
            sendMessage.setReplyMarkup(getOneButtonMarkUp("RESTART \uD83D\uDD04"));
            return sendMessage;
        } else if (answer.equals("CANCEL")) {
            user.setBotState(BotState.START);
            chatIdUsers.put(chatId, user);
            sendMessage.setReplyMarkup(getOneButtonMarkUp("RESTART \uD83D\uDD04"));
            return sendMessage;
        } else {
            sendMessage.setText("Please give the right answer");
            return sendMessage;
        }
    }

    @Override
    public void saveGroups(Update update) {
        String chatId = update.getMessage().getChatId().toString();
        Optional.ofNullable(groups.get(chatId))
                .orElseGet(() -> {
                    Groups group = Groups.builder()
                            .groupId(chatId)
                            .groupName(update.getMessage().getChat().getFirstName())
                            .build();
                    groups.put(chatId, group);
                    return group;
                });
    }

    private String chatId(Update update) {
        return update.getMessage().getChatId().toString();
    }

    @Override
    public User getUserByChatId(String chatId, Update update) {
        return Optional.ofNullable(chatIdUsers.get(chatId))
                .orElseGet(() -> {
                    User user = User.builder()
                            .chatId(chatId)
                            .name(update.getMessage().getFrom().getFirstName())
                            .botState(BotState.START)
                            .build();
                    chatIdUsers.put(chatId, user);
                    return user;
                });
    }

    private ReplyKeyboardMarkup getOneButtonMarkUp(String text) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        replyKeyboardMarkup.setSelective(true);

        List<KeyboardRow> rows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        KeyboardButton button = new KeyboardButton();
        button.setText(text);
        row.add(button);
        rows.add(row);

        replyKeyboardMarkup.setKeyboard(rows);
        return replyKeyboardMarkup;
    }
}
