package com.rekki.botspammer.service;

import com.rekki.botspammer.model.User;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface BotService {

    SendMessage start(Update update);

    SendMessage getMessage(Update update);

    SendMessage getNumberOfMessage(Update update);

    SendMessage sendSpam(Update update);

    User getUserByChatId(String chatId, Update update);

    void saveChannels(Update update);

    void saveGroups(Update update);

}
