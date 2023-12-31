package com.zeydie.telegrambot.modules.cache.messages.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.util.concurrent.AbstractScheduledService;
import com.google.common.util.concurrent.Service;
import com.zeydie.sgson.SGsonFile;
import com.zeydie.telegrambot.TelegramBotCore;
import com.zeydie.telegrambot.api.modules.cache.messages.IMessagesCache;
import com.zeydie.telegrambot.api.modules.cache.messages.data.ListMessagesData;
import com.zeydie.telegrambot.api.modules.cache.messages.data.MessageData;
import com.zeydie.telegrambot.utils.FileUtil;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.zeydie.telegrambot.utils.ReferencePaths.CACHE_MESSAGES_FOLDER_FILE;
import static com.zeydie.telegrambot.utils.ReferencePaths.CACHE_MESSAGES_FOLDER_PATH;

@Log4j2
public class CachingMessagesCacheImpl implements IMessagesCache {
    @Getter
    private final @NotNull Service scheduledService;

    public CachingMessagesCacheImpl() {
        this.scheduledService = new AbstractScheduledService() {
            @Override
            protected void runOneIteration() {
                chatMessageCache.cleanUp();
            }

            @Override
            protected @NotNull Scheduler scheduler() {
                return Scheduler.newFixedRateSchedule(0, 250, TimeUnit.MILLISECONDS);
            }
        }.startAsync();
    }

    private final @NotNull Cache<Long, ListMessagesData> chatMessageCache = CacheBuilder.newBuilder()
            .expireAfterWrite(100, TimeUnit.MILLISECONDS)
            .removalListener((RemovalListener<Long, ListMessagesData>) notification -> {
                        if (notification.getKey() == null) return;
                        if (notification.getValue() == null) return;

                        @NonNull val chatId = notification.getKey();
                        @NonNull val listMessagesData = notification.getValue();
                        @Nullable val messageDatas = listMessagesData.messages();

                        if (messageDatas != null)
                            log.debug("{} {}", chatId, Arrays.toString(messageDatas.toArray()));

                        messageDatas.forEach(messageData -> TelegramBotCore.getInstance().getMessageEventHandler().handle(messageData.message()));
                    }
            ).build();

    @Override
    public void preInit() {
        CACHE_MESSAGES_FOLDER_FILE.mkdirs();
    }

    @SneakyThrows
    @Override
    public void init() {
        @Nullable val files = CACHE_MESSAGES_FOLDER_FILE.listFiles();

        if (files != null)
            Arrays.stream(files)
                    .forEach(file -> {
                                try {
                                    val chatId = Long.parseLong(FileUtil.getFileName(file));
                                    @NonNull final ListMessagesData listMessagesData = new SGsonFile(file).fromJsonToObject(new ListMessagesData(null));
                                    @Nullable final List<MessageData> messages = listMessagesData.messages();

                                    log.info("Chat: {} restored {} messages", chatId, messages.size());
                                    this.chatMessageCache.put(chatId, listMessagesData);
                                } catch (final Exception exception) {
                                    exception.printStackTrace();
                                }
                            }
                    );
    }

    @Override
    public void postInit() {
        this.chatMessageCache.cleanUp();
    }

    @Override
    public void save() {
        this.postInit();

        this.chatMessageCache.asMap()
                .forEach(
                        (chat, message) -> {
                            log.info("Saving message cache for {}", chat);
                            new SGsonFile(FileUtil.createFileWithName(CACHE_MESSAGES_FOLDER_PATH, chat)).writeJsonFile(message);
                        }
                );
    }

    @Override
    public void put(@NonNull final MessageData messageData) {
        val id = messageData.message().chat().id();

        @Nullable var listMessagesData = this.chatMessageCache.getIfPresent(id);

        if (listMessagesData == null) {
            listMessagesData = new ListMessagesData(null);

            this.chatMessageCache.put(id, listMessagesData);
        }

        listMessagesData.add(messageData);
    }
}