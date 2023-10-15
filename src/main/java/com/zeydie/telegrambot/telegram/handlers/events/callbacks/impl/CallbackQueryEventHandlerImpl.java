package com.zeydie.telegrambot.telegram.handlers.events.callbacks.impl;

import com.pengrad.telegrambot.model.CallbackQuery;
import com.zeydie.telegrambot.api.handlers.AbstractEventHandler;
import com.zeydie.telegrambot.api.telegram.events.callback.CallbackQueryEventSubscribe;
import com.zeydie.telegrambot.api.telegram.handlers.events.callbacks.ICallbackQueryEventHandler;
import com.zeydie.telegrambot.telegram.events.callback.CallbackQueryEvent;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.Collections;

@Log4j2
public class CallbackQueryEventHandlerImpl extends AbstractEventHandler implements ICallbackQueryEventHandler {
    @Override
    public @NotNull Class<? extends Annotation> getEventAnnotation() {
        return CallbackQueryEventSubscribe.class;
    }

    @Override
    public @Nullable Class<?>[] getParameters() {
        return Collections.singleton(CallbackQueryEvent.class).toArray(new Class[]{});
    }

    @Override
    public void load() {
        super.load();
    }

    @Override
    public void handle(@NotNull final CallbackQuery callbackQuery) {
        super.invoke(new CallbackQueryEvent(callbackQuery));
    }
}