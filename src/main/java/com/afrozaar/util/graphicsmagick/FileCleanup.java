package com.afrozaar.util.graphicsmagick;

import org.springframework.core.io.FileSystemResource;
import org.springframework.util.WeakReferenceMonitor;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class FileCleanup {
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(FileCleanup.class);

    public static void cleanup(File file) {
        delete(file);
    }

    private static void delete(File file) {
        Objects.requireNonNull(file, "Can not delete null file.");
        try {
            if (file.exists()) {
                LOG.trace("deleting {}", file);
                FileUtils.forceDelete(file);
            }
        } catch (IOException e) {
            LOG.error("error deleting {}", file, e);
        }
    }

    public static void cleanup(FileSystemResource resource) {
        WeakReferenceMonitor.monitor(resource, () -> {
            LOG.trace("released {}", resource.getFile());
            cleanup(resource.getFile());
        });
    }

    public static FileSystemResource withCleanup(FileSystemResource fileSystemResource) {
        cleanup(fileSystemResource);
        return fileSystemResource;
    }

    public static FileSystemResource withCleanup(String source) {
        return withCleanup(new FileSystemResource(source));
    }
}
