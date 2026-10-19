/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.api.file.exception;

import ru.recoin.api.file.exception.FileProcessingException;

public class FileLoadException
extends FileProcessingException {
    public FileLoadException(String message) {
        super(message);
    }

    public FileLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}

