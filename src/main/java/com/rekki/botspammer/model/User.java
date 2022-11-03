package com.rekki.botspammer.model;

import com.rekki.botspammer.enums.BotState;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class User {

    private String chatId;

    private BotState botState;

    private String name;

    private String message;

    private Long msgNums;

}
