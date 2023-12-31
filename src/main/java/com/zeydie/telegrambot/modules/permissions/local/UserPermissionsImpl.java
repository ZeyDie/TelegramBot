package com.zeydie.telegrambot.modules.permissions.local;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.zeydie.sgson.SGsonFile;
import com.zeydie.telegrambot.api.modules.cache.users.data.UserData;
import com.zeydie.telegrambot.api.modules.permissions.IPermissions;
import com.zeydie.telegrambot.api.modules.permissions.data.PermissionData;
import com.zeydie.telegrambot.utils.FileUtil;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

import static com.zeydie.telegrambot.utils.ReferencePaths.*;

@Log4j2
public class UserPermissionsImpl implements IPermissions {
    private final @NotNull Cache<Long, PermissionData> usersPermissionsCache = CacheBuilder.newBuilder().build();

    @Override
    public void preInit() {
        PERMISSIONS_FOLDER_FILE.mkdirs();
    }

    @Override
    public void init() {
        @Nullable val files = PERMISSIONS_FOLDER_FILE.listFiles();

        if (files != null)
            Arrays.stream(files)
                    .forEach(file -> {
                                try {
                                    log.info("Restoring {}", file.getName());

                                    val userId = Long.parseLong(FileUtil.getFileName(file));
                                    @NonNull val permissionData = new SGsonFile(file).fromJsonToObject(new PermissionData(null));

                                    if (permissionData.permissions().isEmpty()) file.delete();
                                    else {
                                        log.info("User {} restored {}", userId, permissionData);
                                        this.usersPermissionsCache.put(userId, permissionData);
                                    }
                                } catch (final Exception exception) {
                                    exception.printStackTrace();
                                }
                            }
                    );
    }

    @Override
    public void postInit() {
        this.usersPermissionsCache.cleanUp();
    }

    @Override
    public void save() {
        this.postInit();

        this.usersPermissionsCache.asMap()
                .forEach(
                        (id, userData) -> {
                            if (userData.permissions().isEmpty()) return;

                            log.info("Saving user data permissions for {}", id);
                            new SGsonFile(FileUtil.createFileWithNameAndType(PERMISSIONS_FOLDER_PATH, id, PERMISSION_TYPE)).writeJsonFile(userData);
                        }
                );
    }

    @Override
    public void addPermission(
            @NonNull final UserData userData,
            @NonNull final String permission
    ) {
        this.addPermission(userData.getUser().id(), permission);
    }

    @Override
    public void addPermission(
            final long chatId,
            @NonNull final String permission
    ) {
        if (this.hasPermission(chatId, permission)) return;

        @Nullable var permissionData = this.getPermissionData(chatId);

        if (permissionData == null) {
            permissionData = new PermissionData(null);

            this.usersPermissionsCache.put(chatId, permissionData);
        }

        permissionData.permissions().add(permission);
    }

    @Override
    public boolean hasPermission(
            @NonNull final UserData userData,
            @NonNull final String permission
    ) {
        return this.hasPermission(userData.getUser().id(), permission);
    }

    @Override
    public boolean hasPermission(
            final long chatId,
            @NonNull final String permission
    ) {
        @Nullable val permissionData = this.getPermissionData(chatId);

        if (permissionData == null)
            return false;

        @NonNull val permissions = permissionData.permissions();

        return permissions.contains("*") || permissions.contains(permission);
    }

    @Override
    public @Nullable PermissionData getPermissionData(@NonNull final UserData userData) {
        return this.getPermissionData(userData.getUser().id());
    }

    @Override
    public @Nullable PermissionData getPermissionData(final long chatId) {
        return this.usersPermissionsCache.getIfPresent(chatId);
    }

    @Override
    public void removePermission(
            @NonNull final UserData userData,
            @NonNull final String permission
    ) {
        this.removePermission(userData.getUser().id(), permission);
    }

    @Override
    public void removePermission(
            final long chatId,
            @NonNull final String permission
    ) {
        @Nullable val permissionData = this.getPermissionData(chatId);

        if (permissionData != null)
            permissionData.permissions().remove(permission);
    }

    @Override
    public void removePermissions(@NonNull final UserData userData) {
        this.removePermissions(userData.getUser().id());
    }

    @Override
    public void removePermissions(final long chatId) {
        this.usersPermissionsCache.asMap().remove(chatId);
    }
}