package com.afrozaar.util.graphicsmagick;

import com.afrozaar.util.graphicsmagick.GraphicsMagicImageIO.XY;

import com.google.common.io.ByteSource;

import java.io.IOException;
import java.util.Map;

public interface IImageService {

    String resize(String tempImageLoc, int maximumHeight, int maximumWidth, String newSuffix, Double imageQuality) throws IOException;

    String resize(String tempImageLoc, int maximumHeight, int maximumWidth, String newSuffix) throws IOException;

    String crop(String templateImageLoc, XY size, XY offsets, XY resizeXY, String newSuffix) throws IOException;

    String crop(String templateImageLoc, XY size, XY offsets, XY resizeXY, String newSuffix, Double imageQuality) throws IOException;

    ByteSource loadImage(String tempImageLoc);

    class ImageInfo {

        private String topName;
        private int width;
        private int height;
        private String mimeType;
        private Map<String, Object> metaData;

        /**
         * @param width    the images width
         * @param height   the images height
         * @param mimeType the mime type of the image
         * @param topName  the name of the image without it's path and without its
         *                 extension
         */
        public ImageInfo(int width, int height, String mimeType, String topName) {
            super();
            this.width = width;
            this.height = height;
            this.mimeType = mimeType;
            this.setTopName(topName);
        }

        /**
         * @return mime type of the image
         */
        public String getMimeType() {
            return mimeType;
        }

        /**
         * @return the images width
         */
        public int getWidth() {
            return width;
        }

        /**
         * @return the images height
         */
        public int getHeight() {
            return height;
        }

        /**
         * @return the "top" name of the image - this is the name of the image
         * without it's extension and without its path
         */
        public String getTopName() {
            return topName;
        }

        public void setTopName(String topName) {
            this.topName = topName;
        }

        public Map<String, Object> getMetaData() {
            return metaData;
        }

        public void setMetaData(Map<String, Object> metaData) {
            this.metaData = metaData;
        }

        @Override
        public String toString() {
            return "ImageInfo [topName=" + topName + ", width=" + width + ", height=" + height + ", mimeType=" + mimeType + "]";
        }
    }

    ImageInfo getImageInfo(String tempImageLoc, boolean includeMeta, MetaDataFormat format) throws IOException;

    String downloadResource(String archiveUrl, ByteSource simpleResource) throws IOException;

    String saveImageToTemp(ByteSource findSimpleResource, String sourceName) throws IOException;

    void cleanup(String downloadResource);

}
