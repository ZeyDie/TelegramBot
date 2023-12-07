package com.zeydie.telegrambot.api.telegram.handlers.events;

import com.pengrad.telegrambot.model.Message;
import com.zeydie.telegrambot.api.modules.cache.messages.data.MessageData;
import com.zeydie.telegrambot.api.modules.interfaces.IInitialize;
import lombok.NonNull;

public interface IMessageEventHandler extends IInitialize {
    void handle(@NonNull final MessageData messageData);

    void handle(@NonNull final Message message);
}