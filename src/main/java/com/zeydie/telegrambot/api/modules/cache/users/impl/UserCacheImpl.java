package com.zeydie.telegrambot.api.modules.cache.users.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.pengrad.telegrambot.model.User;
import com.zeydie.sgson.SGsonFile;
import com.zeydie.telegrambot.api.modules.cache.users.IUserCache;
import com.zeydie.telegrambot.api.modules.cache.users.data.UserData;
import com.zeydie.telegrambot.api.utils.FileUtil;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.zeydie.telegrambot.api.utils.ReferencePaths.CACHE_USERS_FOLDER;

@Log4j2
public class UserCacheImpl implements IUserCache {
    @NotNull
    private final Cache<Long, UserData> userDataCache = CacheBuilder.newBuilder()
            .expireAfterWrite(4, TimeUnit.HOURS)
            .removalListener((RemovalListener<Long, UserData>) notification -> {
                final long userId = notification.getKey();
                final UserData userData = notification.getValue();

                log.debug("Cleanup {} {}", userId, userData);
            })
            .build();

    @SneakyThrows
    @Override
    public void load() {
        CACHE_USERS_FOLDER.toFile().mkdirs();

        Arrays.stream(Objects.requireNonNull(CACHE_USERS_FOLDER.toFile().listFiles()))
                .forEach(file -> {
                    try {
                        log.info("Restoring {}", file.getName());

                        final long userId = Long.parseLong(FileUtil.getFileName(file));
                        final UserData userData = new SGsonFile(file).fromJsonToObject(new UserData(null));

                        log.info("User {} restored {}", userId, userData);

                        this.userDataCache.put(userId, userData);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    @Override
    public void save() {
        CACHE_USERS_FOLDER.toFile().mkdirs();

        this.userDataCache.asMap().forEach((id, userData) -> {
            new SGsonFile(
                    CACHE_USERS_FOLDER.resolve(FileUtil.createFileWithType(id, "json"))
            ).writeJsonFile(userData);

            log.info("Saving user data cache for {}", id);
        });
    }

    @Override
    public boolean contains(@NotNull final User user) {
        return this.contains(user.id());
    }

    @Override
    public boolean contains(final long userId) {
        return this.userDataCache.asMap().containsKey(userId);
    }

    @Override
    public void put(@NotNull final User user) {
        if (!this.contains(user))
            this.userDataCache.put(user.id(), new UserData(user));
    }

    @Nullable
    @Override
    public UserData getUserData(@NotNull final User user) {
        return this.getUserData(user.id());
    }

    @Nullable
    @Override
    public UserData getUserData(final long userId) {
        return this.userDataCache.getIfPresent(userId);
    }
}