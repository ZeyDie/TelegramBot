package com.zeydie.telegrambot.api.modules.cache.users;

import com.pengrad.telegrambot.model.User;
import com.zeydie.telegrambot.api.modules.IData;
import com.zeydie.telegrambot.api.modules.cache.users.data.UserData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IUserCache extends IData {
    boolean contains(@NotNull final UserData userData);

    boolean contains(@NotNull final User user);

    boolean contains(final long userId);

    void put(@NotNull final UserData userData);

    void put(@NotNull final User user);

    @NotNull
    UserData getUserData(@NotNull final User user);

    @Nullable
    UserData getUserData(final long userId);
}