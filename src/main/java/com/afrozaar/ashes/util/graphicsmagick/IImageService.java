package com.afrozaar.ashes.util.graphicsmagick;

import com.google.common.io.ByteSource;

import java.io.IOException;

public interface IImageService {

    String resize(String tempImageLoc, int maximumHeight, int maximumWidth, String newSuffix) throws IOException;

    ByteSource loadImage(String tempImageLoc);

    public class ImageInfo {

        private String topName;

        /**
         *
         * @param width
         *            the images width
         * @param height
         *            the images height
         * @param mimeType
         *            the mime type of the image
         * @param topName
         *            the name of the image without it's path and without its
         *            extension
         */
        public ImageInfo(int width, int height, String mimeType, String topName) {
            super();
            this.width = width;
            this.height = height;
            this.mimeType = mimeType;
            this.setTopName(topName);
        }

        private int width, height;
        private String mimeType;

        /**
         *
         * @return mime type of the image
         */
        public String getMimeType() {
            return mimeType;
        }

        /**
         *
         * @return the images width
         */
        public int getWidth() {
            return width;
        }

        /**
         *
         * @return the images height
         */
        public int getHeight() {
            return height;
        }

        /**
         *
         * @return the "top" name of the iamge - this is the name of the image
         *         without it's extension and without its path
         */
        public String getTopName() {
            return topName;
        }

        public void setTopName(String topName) {
            this.topName = topName;
        }

        @Override
        public String toString() {
            return "ImageInfo [topName=" + topName + ", width=" + width + ", height=" + height + ", mimeType=" + mimeType + "]";
        }
    }

    ImageInfo getImageInfo(String tempImageLoc) throws IOException;

    String downloadResource(String archiveUrl, ByteSource simpleResource) throws IOException;

    String saveImageToTemp(ByteSource findSimpleResource, String sourceName) throws IOException;

    public void cleanup(String downloadResource);

    ImageInfo getImageInfoAndCopyToRepository(ResourceLocation resourceLocation, StorageType storageType, String path, IStorageRepository storageRepository) throws IOException;

}
