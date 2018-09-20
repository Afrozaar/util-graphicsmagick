package com.afrozaar.util.graphicsmagick;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;

import com.afrozaar.util.Regex;
import com.afrozaar.util.graphicsmagick.data.ImageInfo;
import com.afrozaar.util.graphicsmagick.exception.GraphicsMagickException;
import com.afrozaar.util.graphicsmagick.meta.MetaDataFormat;
import com.afrozaar.util.graphicsmagick.meta.MetaParser;
import com.afrozaar.util.graphicsmagick.operation.Convert;
import com.afrozaar.util.graphicsmagick.operation.Identify;
import com.afrozaar.util.graphicsmagick.operation.OutputResult;
import com.afrozaar.util.java8.Functions;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.ByteSource;

import org.springframework.stereotype.Component;

import org.gm4java.engine.GMService;
import org.gm4java.im4java.GMBatchCommand;
import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;
import org.im4java.core.Operation;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

@Component
public class GraphicsMagickImageIO extends AbstractImageIO {

    private GMService service;

    public GraphicsMagickImageIO() {
        this.service = new PooledGmServiceConfig().getGraphicsMagickService();
    }

    public GraphicsMagickImageIO(GMService service) {
        this.service = service;
    }

    public static class XY {
        private int x;
        private int y;

        public XY(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        @Override
        public String toString() {
            return "[" + x + "x" + y + "]";
        }
    }

    @Override
    public String resize(final String tempImageLoc, int maximumWidth, int maximumHeight, String newSuffix) throws IOException {
        return resize(tempImageLoc, maximumWidth, maximumHeight, newSuffix, null);
    }

    @Override
    public String resize(String tempImageLoc, int maximumWidth, int maximumHeight, @Nullable String newSuffix, @Nullable Double imageQuality)
            throws IOException {
        String interlace = "Line";
        boolean deleteTemp = false;
        if (tempImageLoc.endsWith(".gif")) {
            tempImageLoc = coalesce(tempImageLoc);
            interlace = "None";
            deleteTemp = true;
        }

        ImageInfo imageInfo = getImageInfo(tempImageLoc, false, null);
        Operation operation = Convert.createImOperation(tempImageLoc, imageInfo, imageQuality);
        ((IMOperation) operation).resize(maximumWidth, maximumHeight, ">").interlace(interlace);
        ofNullable(newSuffix).ifPresent(suffix -> ((IMOperation) operation).background("white").flatten());

        // execute the operation
        try {
            final String outputFileName = getOutputFileName(tempImageLoc, newSuffix);
            operation.addImage(outputFileName);
            runOperation(Convert.COMMAND, operation);
            return outputFileName;
        } catch (InterruptedException | IM4JavaException | URISyntaxException e) {
            throw new IOException(e);
        } finally {
            if (deleteTemp) {
                FileCleanup.delete(tempImageLoc);
            }
        }
    }

    @Override
    public String crop(String tempImageLoc, XY size, XY offsets, @Nullable XY resizeXY, @Nullable String newSuffix) throws IOException {
        return crop(tempImageLoc, size, offsets, resizeXY, newSuffix, null);
    }

    @Override
    public String crop(String tempImageLoc, XY size, XY offsets, @Nullable XY resizeXY, @Nullable String newSuffix, @Nullable Double imageQuality)
            throws IOException {
        String interlace = "Line";
        boolean deleteTemp = false;
        if (tempImageLoc.endsWith(".gif")) {
            tempImageLoc = coalesce(tempImageLoc);
            interlace = "None";
            deleteTemp = true;
        }

        ImageInfo imageInfo = getImageInfo(tempImageLoc, false, null);
        Operation operation = Convert.createImOperation(tempImageLoc, imageInfo, imageQuality);
        ((IMOperation) operation).crop(size.getX(), size.getY(), offsets.getX(), offsets.getY()).interlace(interlace);
        ofNullable(resizeXY).ifPresent(xy -> ((IMOperation) operation).resize(xy.getX(), xy.getY(), ">"));
        ofNullable(newSuffix).ifPresent(suffix -> ((IMOperation) operation).background("white").flatten());

        // execute the operation
        String outputFileName = null;
        try {
            outputFileName = getOutputFileName(tempImageLoc, newSuffix);
            operation.addImage(outputFileName);
            runOperation(Convert.COMMAND, operation);
            return outputFileName;
        } catch (InterruptedException | IM4JavaException | URISyntaxException e) {
            throw new GraphicsMagickException(outputFileName, e.getMessage(), e);
        } finally {
            if (deleteTemp) {
                FileCleanup.delete(tempImageLoc);
            }
        }
    }

    private String coalesce(final String tempImageLoc) throws IOException {
        ImageInfo imageInfo = getImageInfo(tempImageLoc, false, null);
        Operation operation = Convert.createImOperation(tempImageLoc, imageInfo, null);
        ((IMOperation) operation).coalesce();

        // execute the operation
        try {
            final String outputFileName = getOutputFileName(tempImageLoc, null);
            operation.addImage(outputFileName);
            runOperation(Convert.COMMAND, operation);
            return outputFileName;
        } catch (InterruptedException | IM4JavaException | URISyntaxException e) {
            throw new IOException(e);
        }
    }

    private void runOperation(String command, Operation op) throws InterruptedException, IOException, IM4JavaException {
        final Long start = System.currentTimeMillis();
        final GMBatchCommand batchCommand = new GMBatchCommand(service, command);
        final String uuid = UUID.randomUUID().toString();

        LOG.debug("Operation starting: {} {} (trace={})", command, op, uuid);
        batchCommand.run(op);
        LOG.debug("Operation complete: {} {} took {} (trace={})", command, op, Duration.of(System.currentTimeMillis() - start, ChronoUnit.MILLIS), uuid);
    }

    private String runOperationWithOutput(String command, Operation operation) throws InterruptedException, IOException, IM4JavaException {
        final Long start = System.currentTimeMillis();
        final GMBatchCommand batchCommand = new GMBatchCommand(service, command);
        final OutputResult outputResult = new OutputResult();
        batchCommand.setOutputConsumer(outputResult);

        batchCommand.run(operation);
        LOG.debug("Operation with output: {} {} took {}", command, operation, Duration.of(System.currentTimeMillis() - start, ChronoUnit.MILLIS));

        return outputResult.getOutput();
    }

    private String getOutputFileName(String tempImageLoc, @Nullable String suffix) throws URISyntaxException {
        Optional<String> tempSuffix = getExtensionFromFile(tempImageLoc);
        final String name = tempSuffix
                .map(ts -> tempImageLoc.substring(0, tempImageLoc.indexOf(ts) - 1))
                .orElse(tempImageLoc);

        return format("%s_resize%d%s", name, random.nextInt(3), normaliseSuffix0(tempSuffix, suffix));
    }

    private String normaliseSuffix0(Optional<String> tempSuffix, @Nullable String suffix) {
        final String suffixOrTempSuffix = ofNullable(suffix).orElse(tempSuffix.orElse(""));
        final String prependedOrNull = !Strings.isNullOrEmpty(suffixOrTempSuffix) && !suffixOrTempSuffix.startsWith(".")
                ? "." + suffixOrTempSuffix
                : suffixOrTempSuffix;

        return ofNullable(prependedOrNull).orElse("").toLowerCase();
    }

    @Override
    public ByteSource loadImage(final String tempImageLoc) {
        return new ByteSource() {

            @Override
            public InputStream openStream() throws IOException {
                return new BufferedInputStream(new FileInputStream(tempImageLoc));
            }
        };
    }

    @Override
    public ImageInfo getImageInfo(final String tempImageLoc, boolean includeMeta, MetaDataFormat format) throws IOException {
        try {
            final String execute = runOperationWithOutput(Identify.COMMAND, Identify.identify(tempImageLoc));

            //LOG.debug("result from gm: {}", Arrays.asList(split2));

            Integer width = Regex.extractMatch("\\s*width=(\\d+)\\s*", execute).flatMap(Functions.emptyIfExceptionOther(Integer::parseInt)).orElse(null);
            Integer height = Regex.extractMatch("\\s*height=(\\d+)\\s*", execute).flatMap(Functions.emptyIfExceptionOther(Integer::parseInt)).orElse(null);
            Optional<String> type = Regex.extractMatch("\\s*type=(\\w+)\\s*", execute);

            if (width == null || height == null || !type.isPresent()) {
                throw new IllegalArgumentException("error parsing result from graphics magick: " + execute + " cannot get image info");
            }
            final String mimeType = type
                    .map(t -> "image/" + t.toLowerCase())
                    .orElse("application/octet-stream");
            final String topName = Regex.extractMatch("\\s*name=(\\w+)\\s*", execute).orElse(null);

            boolean multiFrame = Regex.isMatch("([\\S\\s]*width[\\s\\S]*){2,}", execute);

            ImageInfo imageInfo = new ImageInfo(width, height, mimeType, topName);
            imageInfo.setMultiFrame(multiFrame);
            if (includeMeta) {
                getImageMetaData(tempImageLoc, format).ifPresent(imageInfo::setMetaData);
            }

            LOG.debug("returning image info {} for url {}", imageInfo, tempImageLoc);
            return imageInfo;
        } catch (IM4JavaException | InterruptedException e) {
            throw new GraphicsMagickException(null, e.getMessage(), e);
        }
    }

    private Optional<Map<String, Object>> getImageMetaData(final String tempImageLoc, @Nullable MetaDataFormat format) {
        // gm identify -verbose ~/Pictures/nikon/nikon_20160214/darktable_exported/img_0001.jpg
        try {
            final String execute = runOperationWithOutput(Identify.COMMAND, Identify.identifyVerbose(tempImageLoc));

            if (format == MetaDataFormat.RAW) {
                return Optional.of(ImmutableMap.of("data", execute));
            }

            return MetaParser.parseResult(execute);
        } catch (IOException | InterruptedException | IM4JavaException e) {
            LOG.error("Error ", e);
            return Optional.empty();
        }
    }

    @Override
    public String saveImageToTemp(ByteSource findSimpleResource, String sourceName) throws IOException, URISyntaxException {
        final String sourceNameToUse = ofNullable(sourceName).orElse(getRandomAlpha(10));

        FileOutputStream output = null;
        final String tempFileName = getTempFileName(sourceNameToUse);
        try {
            File file = new File(tempFileName);
            output = new FileOutputStream(file);
            findSimpleResource.copyTo(output);
            return file.getAbsolutePath();
        } catch (IOException e) {
            FileCleanup.withCleanup(tempFileName);
            throw e;
        } finally {
            if (output != null) {
                output.close();
            }
        }
    }

    @Override
    public void cleanup(String downloadResource) {
        final File fileToDelete = new File(downloadResource);
        if (fileToDelete.exists() && !fileToDelete.delete()) {
            LOG.warn("Could not clean up existing file: {}", fileToDelete);
        }
    }

}
