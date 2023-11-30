package com.zeydie.telegrambot.api.telegram.events;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.*;

@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CallbackQueryEventSubscribe {
    @NotNull String[] callbackDatas();

    boolean startWith() default false;

    boolean endWith() default false;

    @Nullable String comment() default "";
}