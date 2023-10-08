package com.zeydie.telegrambot.api.modules.cache.messages.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.util.concurrent.AbstractScheduledService;
import com.google.common.util.concurrent.Service;
import com.pengrad.telegrambot.model.Message;
import com.zeydie.sgson.SGsonFile;
import com.zeydie.telegrambot.api.modules.cache.messages.IMessagesCache;
import com.zeydie.telegrambot.api.modules.cache.messages.data.ChatMessagesData;
import com.zeydie.telegrambot.api.utils.FileUtil;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.zeydie.telegrambot.api.utils.ReferencePaths.CACHE_MESSAGES_FOLDER;

@Log4j2
public class CachingMessagesCacheImpl implements IMessagesCache {
    @Getter
    @NotNull
    private final Service scheduledService;

    public CachingMessagesCacheImpl() {
        this.scheduledService = new AbstractScheduledService() {
            @Override
            protected void runOneIteration() {
                chatMessageCache.cleanUp();
            }

            @NotNull
            @Override
            protected Scheduler scheduler() {
                return Scheduler.newFixedRateSchedule(0, 250, TimeUnit.MILLISECONDS);
            }
        }.startAsync();
    }

    @NotNull
    private final Cache<Long, List<Message>> chatMessageCache = CacheBuilder.newBuilder()
            .expireAfterWrite(100, TimeUnit.MILLISECONDS)
            .removalListener((RemovalListener<Long, List<Message>>) notification -> {
                final long chatId = notification.getKey();
                final List<Message> messages = notification.getValue();

                log.debug("{} {}", chatId, Arrays.toString(Objects.requireNonNull(messages).toArray()));

                //TODO Processing message
            })
            .build();

    @SneakyThrows
    @Override
    public void load() {
        CACHE_MESSAGES_FOLDER.toFile().mkdirs();

        Arrays.stream(Objects.requireNonNull(CACHE_MESSAGES_FOLDER.toFile().listFiles()))
                .forEach(file -> {
                    try {
                        final long chatId = Long.parseLong(FileUtil.getFileName(file));
                        final ChatMessagesData chatMessagesData = new SGsonFile(file).fromJsonToObject(new ChatMessagesData(new ArrayList<>()));

                        final List<Message> messages = chatMessagesData.messages();

                        log.info(String.format("Chat: %d restored %d messages", chatId, messages.size()));

                        this.chatMessageCache.put(chatId, messages);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    @Override
    public void save() {
        CACHE_MESSAGES_FOLDER.toFile().mkdirs();

        this.chatMessageCache.asMap().forEach((chat, message) -> {
            final SGsonFile gsonFile = new SGsonFile(CACHE_MESSAGES_FOLDER.resolve(String.valueOf(chat)));
            final ChatMessagesData chatMessagesData = gsonFile.fromJsonToObject(new ChatMessagesData(new ArrayList<>()));

            chatMessagesData.messages().addAll(message);

            gsonFile.writeJsonFile(chatMessagesData);

            log.info(String.format("Saving message cache for %d", chat));
        });
    }

    @Override
    public void put(@NotNull final Message message) {
        final long id = message.chat().id();

        List<Message> messages = this.chatMessageCache.getIfPresent(id);

        if (messages == null) {
            messages = new ArrayList<>();

            this.chatMessageCache.put(id, messages);
        }

        messages.add(message);
    }
}