package com.afrozaar.util.graphicsmagick.api;

import com.afrozaar.util.graphicsmagick.GraphicsMagickImageIO.XY;
import com.afrozaar.util.graphicsmagick.data.ImageInfo;
import com.afrozaar.util.graphicsmagick.meta.MetaDataFormat;

import com.google.common.io.ByteSource;

import java.io.IOException;

public interface IImageService {

    String resize(String tempImageLoc, int maximumHeight, int maximumWidth, String newSuffix, Double imageQuality) throws IOException;

    String resize(String tempImageLoc, int maximumHeight, int maximumWidth, String newSuffix) throws IOException;

    String crop(String templateImageLoc, XY size, XY offsets, XY resizeXY, String newSuffix) throws IOException;

    String crop(String templateImageLoc, XY size, XY offsets, XY resizeXY, String newSuffix, Double imageQuality) throws IOException;

    ByteSource loadImage(String tempImageLoc);

    ImageInfo getImageInfo(String tempImageLoc, boolean includeMeta, MetaDataFormat format) throws IOException;

    String downloadResource(String archiveUrl, ByteSource simpleResource) throws IOException;

    String saveImageToTemp(ByteSource findSimpleResource, String sourceName) throws IOException;

    void cleanup(String downloadResource);

}
