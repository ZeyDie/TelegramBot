package com.zeydie.telegrambot.api.telegram.handlers.events;

import com.pengrad.telegrambot.model.Message;
import com.zeydie.telegrambot.api.modules.interfaces.IInitialize;
import lombok.NonNull;

public interface ICommandEventHandler extends IInitialize {
    void handle(@NonNull final Message message);
}