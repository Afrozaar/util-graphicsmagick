package com.afrozaar.util.graphicsmagick.api;

import com.afrozaar.util.graphicsmagick.GraphicsMagickImageIO.XY;
import com.afrozaar.util.graphicsmagick.data.ImageInfo;
import com.afrozaar.util.graphicsmagick.meta.MetaDataFormat;
import com.google.common.io.ByteSource;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URISyntaxException;

public interface IImageService {

    String resize(String tempImageLoc, int maximumHeight, int maximumWidth, @Nullable String newSuffix, @Nullable Double imageQuality) throws IOException;

    String resize(String tempImageLoc, int maximumHeight, int maximumWidth, @Nullable String newSuffix) throws IOException;

    String crop(String templateImageLoc, XY size, XY offsets, XY resizeXY, String newSuffix) throws IOException;

    String crop(String templateImageLoc, XY size, XY offsets, XY resizeXY, String newSuffix, @Nullable Double imageQuality) throws IOException;

    ByteSource loadImage(String tempImageLoc);

    ImageInfo getImageInfo(String tempImageLoc, boolean includeMeta, MetaDataFormat format) throws IOException;

    String downloadResource(String archiveUrl, ByteSource simpleResource) throws IOException, URISyntaxException;

    String saveImageToTemp(ByteSource findSimpleResource, String sourceName) throws IOException, URISyntaxException;

    void cleanup(String downloadResource);

}
