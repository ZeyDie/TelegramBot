package com.zeydie.telegrambot.exceptions;

import com.zeydie.telegrambot.api.modules.language.data.LanguageData;
import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

public final class LanguageRegisteredException extends Exception {
    @Getter
    private final @NotNull String message;

    public LanguageRegisteredException(@NonNull final LanguageData languageData) {
        this(languageData.uniqueId(), languageData.label());
    }

    public LanguageRegisteredException(
            @NonNull final String uniqueId,
            @NonNull final String label
    ) {
        this.message = String.format("%s (%s) is already registered!", label, uniqueId);
    }
}