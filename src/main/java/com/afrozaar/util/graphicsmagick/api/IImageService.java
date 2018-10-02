package com.afrozaar.util.graphicsmagick.api;

import com.afrozaar.util.graphicsmagick.GraphicsMagickImageIO.XY;
import com.afrozaar.util.graphicsmagick.data.ImageInfo;
import com.afrozaar.util.graphicsmagick.meta.MetaDataFormat;
import com.afrozaar.util.graphicsmagick.operation.Flag;

import com.google.common.io.ByteSource;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.EnumSet;

import javax.annotation.Nullable;

public interface IImageService {

    default String resize(String tempImageLoc, int maximumHeight, int maximumWidth, @Nullable String newSuffix, @Nullable Double imageQuality)
            throws IOException {
        return resize(tempImageLoc, maximumHeight, maximumWidth, newSuffix, imageQuality, EnumSet.noneOf(Flag.class));
    }

    String resize(String tempImageLoc, int maximumHeight, int maximumWidth, @Nullable String newSuffix, @Nullable Double imageQuality, EnumSet<Flag> flags)
            throws IOException;

    default String resize(final String tempImageLoc, int maximumWidth, int maximumHeight, String newSuffix) throws IOException {
        return resize(tempImageLoc, maximumWidth, maximumHeight, newSuffix, null, EnumSet.noneOf(Flag.class));
    }

    default String crop(String templateImageLoc, XY size, XY offsets, XY resizeXY, String newSuffix) throws IOException {
        return crop(templateImageLoc, size, offsets, resizeXY, newSuffix, null, EnumSet.noneOf(Flag.class));
    }

    default String crop(String templateImageLoc, XY size, XY offsets, XY resizeXY, String newSuffix, @Nullable Double imageQuality) throws IOException {
        return crop(templateImageLoc, size, offsets, resizeXY, newSuffix, imageQuality, EnumSet.noneOf(Flag.class));
    }

    String crop(String templateImageLoc, XY size, XY offsets, XY resizeXY, String newSuffix, @Nullable Double imageQuality, EnumSet<Flag> flags)
            throws IOException;

    ByteSource loadImage(String tempImageLoc);

    ImageInfo getImageInfo(String tempImageLoc, boolean includeMeta, MetaDataFormat format) throws IOException;

    String downloadResource(String archiveUrl, ByteSource simpleResource) throws IOException, URISyntaxException;

    String saveImageToTemp(ByteSource findSimpleResource, String sourceName) throws IOException, URISyntaxException;

    void cleanup(String downloadResource);

}
