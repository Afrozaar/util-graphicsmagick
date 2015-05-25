package com.afrozaar.ashes.util.graphicsmagick;

import com.google.common.io.ByteSource;

import org.springframework.beans.factory.annotation.Value;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

public abstract class AbstractImageIO implements IImageService {

    protected final Logger LOG = LoggerFactory.getLogger(getClass());
    @Inject
    @Named("secureStorageRepository")
    ISecureStorageRepository storage;

    private String tempDir;
    private String tempDirProperty;
    private boolean tempDirSet;

    @Value("${tempDir}")
    public void setTempDir(String tempDir) {
        this.tempDirProperty = tempDir;
        this.tempDirSet = true;
    }

    @PostConstruct
    public void setTempDir() {
        if (tempDirSet && tempDirProperty != null && !tempDirProperty.equals("${tempDir}")) {
            this.tempDir = tempDirProperty;
        } else {
            this.tempDir = System.getProperty("java.io.tmpdir") + "/media";
        }

        final File dir = new File(tempDir);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new RuntimeException(String.format("AbstractImageIO could not prepare the tempDir (Directory did not exist, and could not be created). tempDir=%s", this.tempDir));
        }
    }

    @Override
    public String downloadResource(final String archiveUrl, ByteSource simpleResource) throws IOException {
        if (storage.exists(StorageType.ARCHIVE, archiveUrl)) {
            LOG.info("image {} already exists - not processing", archiveUrl);
            return saveImageToTemp(new ByteSource() {

                @Override
                public InputStream openStream() throws IOException {
                    return storage.getInputStream(StorageType.ARCHIVE, archiveUrl);
                }
            }, archiveUrl);
        } else {
            return saveImageToTemp(simpleResource, archiveUrl);
        }
    }

    protected String getTempFileName(String sourceName) {
        String suffix = getExtension(sourceName);
        return tempDir + File.separator + "resource_" + getRandomAlpha(5) + suffix;
    }

    protected String getExtension(String imageName) {
        int indexOf = imageName.lastIndexOf(".");
        if (indexOf != -1) {
            return imageName.substring(indexOf);
        } else {
            return null;
        }
    }

    static char[] alphabet = new char[26];
    {
        for (int i = 0; i < 26; i++) {
            alphabet[i] = (char) ('A' + i);
        }
    }

    Random random = new Random();

    private String getRandomAlpha(int i) {
        char[] letters = new char[i];
        for (int k = 0; k < i; k++) {
            letters[k] = alphabet[random.nextInt(26)];
        }
        return new String(letters);
    }

    @Scheduled(cron = "1 0 * * * *")
    public void cleanupTempDir() {
        try {
            FileUtils.cleanDirectory(new File(tempDir));
        } catch (IOException e) {
            LOG.error("error cleaning directory {}", tempDir);
        }
    }

    @Override
    public ImageInfo getImageInfoAndCopyToRepository(ResourceLocation resourceLocation, StorageType storageType, String destPath, IStorageRepository storageRepository)
            throws IOException {

        final String tempImageLoc = saveImageToTemp(resourceLocation.getByteSource(), resourceLocation.getPath());
        final ExtendedResourceLocation fileSystemLocation = new FileSystemLocation(storageType, tempImageLoc, new File(tempImageLoc));

        final ImageInfo imageInfo = getImageInfo(tempImageLoc);
        storageRepository.copy(fileSystemLocation, StorageType.IMAGES, destPath, imageInfo.getMimeType());
        cleanup(tempImageLoc); // TODO: manual might not be required as there is a scheduled cleanup
        return imageInfo;
    }
}
