package com.carloship.craft.sdk.utils;


import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class FileUtils {

    public static File getFileFromResource(String resourcePath) {
        URL resourceUrl = FileUtils.class.getClassLoader().getResource(resourcePath);

        if (resourceUrl == null) {
            throw new IllegalArgumentException("Resource not found: " + resourcePath);
        }

        return new File(resourceUrl.getFile());
    }


    public static @NotNull File getOrCreateFile(String folder, String fileName) {
        File directory = new File(folder);
        File file = new File(directory, fileName);

        try {
            if (!directory.exists() && !directory.mkdirs()) {
                throw new IllegalStateException("Could not create directory: " + directory);
            }

            if (!file.exists() && !file.createNewFile()) {
                throw new IllegalStateException("Could not create file: " + file);
            }

            return file;
        } catch (IOException e) {
            throw new IllegalStateException("Could not create file: " + file, e);
        }
    }

}
