/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.api.file.exception;

import ru.recoin.api.file.exception.FileProcessingException;

public class FileSaveException
extends FileProcessingException {
    public FileSaveException(String message) {
        super(message);
    }

    public FileSaveException(String message, Throwable cause) {
        super(message, cause);
    }
}

