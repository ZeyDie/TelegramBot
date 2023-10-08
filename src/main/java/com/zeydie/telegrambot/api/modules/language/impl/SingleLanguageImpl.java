package com.zeydie.telegrambot.api.modules.language.impl;

import com.zeydie.sgson.SGsonFile;
import com.zeydie.telegrambot.api.configs.AbstractFileConfig;
import com.zeydie.telegrambot.api.configs.BotChatFileConfig;
import com.zeydie.telegrambot.api.exceptions.LanguageRegisteredException;
import com.zeydie.telegrambot.api.modules.language.ILanguage;
import com.zeydie.telegrambot.api.modules.language.data.LanguageData;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.zeydie.telegrambot.api.utils.ReferencePaths.LANGUAGE_FOLDER;

@Log4j2
public class SingleLanguageImpl implements ILanguage {
    private LanguageData defaultLanguage;

    @Override
    public void init() throws LanguageRegisteredException {
        final String defaultLanguageId = BotChatFileConfig.getJson().getDefaultLanguageId();

        Arrays.stream(Objects.requireNonNull(LANGUAGE_FOLDER.toFile().listFiles()))
                .forEach(file -> {
                    try {
                        final LanguageData languageData = new SGsonFile(file).fromJsonToObject(
                                new LanguageData(
                                        null,
                                        null,
                                        null
                                )
                        );

                        if (languageData.uniqueId().equals(defaultLanguageId))
                            this.register(languageData);
                    } catch (LanguageRegisteredException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    @Override
    public boolean register(@NotNull final LanguageData languageData) throws LanguageRegisteredException {
        final String label = languageData.label();
        final String uniqueId = languageData.uniqueId();

        if (isRegistered(languageData))
            throw new LanguageRegisteredException(uniqueId, label);

        this.defaultLanguage = initLangFile(languageData);

        log.info("{} ({}) was registered!", label, uniqueId);

        return true;
    }

    @NotNull
    @Override
    public List<LanguageData> getRegisteredLanguages() {
        return Collections.singletonList(this.defaultLanguage);
    }

    @Override
    public boolean isRegistered(@NotNull final LanguageData languageData) {
        return this.isRegistered(languageData.uniqueId());
    }

    @Override
    public boolean isRegistered(@NotNull final String uniqueId) {
        return this.defaultLanguage != null && this.defaultLanguage.uniqueId().equals(uniqueId);
    }

    @Override
    public @Nullable LanguageData getLanguageData(@Nullable final LanguageData languageData) {
        return this.defaultLanguage;
    }

    @Override
    public @Nullable LanguageData getLanguageData(@Nullable final String uniqueId) {
        return this.defaultLanguage;
    }

    public static @NotNull LanguageData initLangFile(@NotNull final LanguageData languageData) {
        return new AbstractFileConfig(
                LANGUAGE_FOLDER,
                languageData,
                languageData.uniqueId()
        ).init();
    }
}