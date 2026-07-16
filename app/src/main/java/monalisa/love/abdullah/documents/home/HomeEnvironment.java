/*
 * Copyright (c) 2021 2bllw8
 * SPDX-License-Identifier: GPL-3.0-only
 */
package monalisa.love.abdullah.documents.home;

import android.content.Context;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class HomeEnvironment {
    public static final String AUTHORITY = "monalisa.love.abdullah.documents";

    public static final String ROOT = "anemo";
    public static final String ROOT_DOC_ID = "root";
    public static final String VAULT_ROOT = "vault";
    public static final String VAULT_ROOT_DOC_ID = "vault_root";

    private final Path baseDir;
    private final Path fallbackDir;

    private static volatile HomeEnvironment instance;

    public static HomeEnvironment getInstance(Context context) throws IOException {
        if (instance == null) {
            synchronized (HomeEnvironment.class) {
                if (instance == null) {
                    instance = new HomeEnvironment(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    private HomeEnvironment(Context context) throws IOException {
        baseDir = context.getFilesDir().toPath().resolve(ROOT);
        if (!Files.exists(baseDir)) {
            Files.createDirectory(baseDir);
        } else if (!Files.isDirectory(baseDir)) {
            throw new IOException(baseDir + " is not a directory");
        }

        fallbackDir = context.getFilesDir().toPath().resolve(VAULT_ROOT);
        if (!Files.exists(fallbackDir)) {
            Files.createDirectory(fallbackDir);
        } else if (!Files.isDirectory(fallbackDir)) {
            throw new IOException(fallbackDir + " is not a directory");
        }
    }

    public Path getBaseDir() {
        return baseDir;
    }

    public boolean isRoot(Path path) {
        return baseDir.equals(path);
    }

    public Path getFallbackDir() {
        return fallbackDir;
    }

    public boolean isFallbackRoot(Path path) {
        return fallbackDir.equals(path);
    }
}
