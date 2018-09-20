package com.afrozaar.util.graphicsmagick;

import static java.util.Optional.ofNullable;

import org.springframework.core.io.FileSystemResource;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileCleanup {
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(FileCleanup.class);

    private static ExecutorService fileDelete = Executors.newCachedThreadPool();

    private static final ReferenceQueue<FileSystemResource> referenceQueue = new ReferenceQueue<>();

    static {

        Thread thread = new Thread(() -> {
            while (true) {
                try {
                    Reference<? extends FileSystemResource> remove = referenceQueue.remove(0);
                    LOG.debug("Deleting file after phantom reference: {}", remove);
                    ofNullable(remove.get()).ifPresent(reference -> delete(reference.getFile()));
                } catch (IllegalArgumentException | InterruptedException e) {
                    LOG.error("error deleting file ", e);
                }
            }
        });
        thread.start();

    }

    public static void cleanup(File file) {
        delete(file);
    }

    private static void delete(File file) {
        Objects.requireNonNull(file, "Can not delete null file.");
        try {
            if (file.exists()) {
                LOG.info("deleting {}", file);
                FileUtils.forceDelete(file);
            }
        } catch (IOException e) {
            LOG.error("error deleting {}", file, e);
        }
    }

    public static void cleanup(FileSystemResource resource) {
        LOG.info("creating phantom reference");
        new PhantomReference<>(resource, referenceQueue);
    }

    public static FileSystemResource withCleanup(FileSystemResource fileSystemResource) {
        cleanup(fileSystemResource);
        return fileSystemResource;
    }

    public static FileSystemResource withCleanup(String source) {
        return withCleanup(new FileSystemResource(source));
    }

    public static void delete(String tempImageLoc) {
        fileDelete.submit(() -> delete(new File(tempImageLoc)));
    }
}
