package com.afrozaar.util.graphicsmagick;

import com.afrozaar.util.graphicsmagick.data.ImageInfo;
import com.afrozaar.util.graphicsmagick.exception.GraphicsMagickException;
import com.afrozaar.util.graphicsmagick.meta.MetaDataFormat;
import com.afrozaar.util.graphicsmagick.meta.MetaParser;
import com.afrozaar.util.graphicsmagick.mime.MimeService;
import com.afrozaar.util.graphicsmagick.operation.Convert;
import com.afrozaar.util.graphicsmagick.operation.Identify;
import com.afrozaar.util.graphicsmagick.operation.OutputResult;
import com.afrozaar.util.graphicsmagick.util.RuntimeLimits;
import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.ByteSource;
import org.gm4java.engine.GMService;
import org.gm4java.engine.support.GMConnectionPoolConfig;
import org.gm4java.engine.support.PooledGMService;
import org.gm4java.im4java.GMBatchCommand;
import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;
import org.im4java.core.Operation;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.io.*;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;

@Component
public class GraphicsMagickImageIO extends AbstractImageIO {

    public GraphicsMagickImageIO() {
        super();
    }

    public GraphicsMagickImageIO(String tempDir) {
        super(tempDir);
    }

    private GMConnectionPoolConfig config = new GMConnectionPoolConfig();

    {
        if (RuntimeLimits.applyLimits()) {
            config.setMaxIdle(4);
            config.setMaxActive(4);
        }

        LOG.info("Initialised GraphicsMagickImageIO with config: {}", MoreObjects.toStringHelper(config)
                .add("maxIdle", config.getMaxIdle())
                .add("maxActive", config.getMaxActive())
                .toString());
    }

    private GMService service = new PooledGMService(config);
    private MimeService mimeService = new MimeService();

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
    public String resize(final String tempImageLoc, int maximumWidth, int maximumHeight, @Nullable String newSuffix, @Nullable Double imageQuality) throws IOException {

        ImageInfo imageInfo = getImageInfo(tempImageLoc, false, null);
        Operation operation = Convert.createImOperation(tempImageLoc, imageInfo, imageQuality);
        ((IMOperation) operation).resize(maximumWidth, maximumHeight, ">").interlace("Line");
        ofNullable(newSuffix).ifPresent(suffix -> ((IMOperation) operation).background("white").flatten());

        final String outputFileName = getOutputFileName(tempImageLoc, newSuffix);
        operation.addImage(outputFileName);

        // execute the operation
        try {
            runOperation(Convert.COMMAND, operation);
            return outputFileName;
        } catch (InterruptedException | IM4JavaException e) {
            throw new IOException(e);
        }
    }


    @Override
    public String crop(String tempImageLoc, XY size, XY offsets, @Nullable XY resizeXY, @Nullable String newSuffix) throws IOException {
        return crop(tempImageLoc, size, offsets, resizeXY, newSuffix, null);
    }

    @Override
    public String crop(String tempImageLoc, XY size, XY offsets, @Nullable XY resizeXY, @Nullable String newSuffix, @Nullable Double imageQuality) throws IOException {

        ImageInfo imageInfo = getImageInfo(tempImageLoc, false, null);
        Operation operation = Convert.createImOperation(tempImageLoc, imageInfo, imageQuality);
        ((IMOperation) operation).crop(size.getX(), size.getY(), offsets.getX(), offsets.getY()).interlace("Line");
        ofNullable(resizeXY).ifPresent(xy -> ((IMOperation) operation).resize(xy.getX(), xy.getY(), ">"));
        ofNullable(newSuffix).ifPresent(suffix -> ((IMOperation) operation).background("white").flatten());

        String outputFileName = getOutputFileName(tempImageLoc, newSuffix);
        operation.addImage(outputFileName);

        // execute the operation
        try {
            runOperation(Convert.COMMAND, operation);
            return outputFileName;
        } catch (InterruptedException | IM4JavaException e) {
            throw new GraphicsMagickException(outputFileName, e.getMessage(), e);
        }
    }

    private void runOperation(String command, Operation op) throws InterruptedException, IOException, IM4JavaException {
        final Long start = System.currentTimeMillis();
        final GMBatchCommand batchCommand = new GMBatchCommand(service, command);
        final String uuid = UUID.randomUUID().toString();

        LOG.debug("Operation starting: {} {} (trace={})", command, op, uuid);
        batchCommand.run(op);
        LOG.info("Operation complete: {} {} took {} (trace={})", command, op, Duration.of(System.currentTimeMillis() - start, ChronoUnit.MILLIS), uuid);
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

    private String getOutputFileName(String tempImageLoc, @Nullable String suffix) {
        String tempSuffix = getExtension(tempImageLoc);
        final String name = ofNullable(tempSuffix)
                .map(ts -> tempImageLoc.substring(0, tempImageLoc.indexOf(ts) - 1))
                .orElse(tempImageLoc);

        return format("%s_resize%d%s", name, random.nextInt(3), normaliseSuffix0(tempSuffix, suffix));
    }

    private String normaliseSuffix0(String tempSuffix, @Nullable String suffix) {
        final String suffixOrTempSuffix = ofNullable(suffix).orElse(tempSuffix);
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

            String[] split2 = execute.split("\n");
            LOG.debug("result from gm: {}", Arrays.asList(split2));

            Map<String, String> split = Arrays.stream(split2)
                    .filter(x -> x.contains("="))
                    .map(x -> {
                        String[] keyValue = x.split("=");
                        return new AbstractMap.SimpleEntry<>(keyValue[0], keyValue[1]);
                    })
                    .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (a, b) -> a));
            // when calling info on gifs we get width and height per image, all the widths and heights are going to be the same

            final int width = Integer.parseInt(split.get("width"));
            final int height = Integer.parseInt(split.get("height"));

            final String mimeType = ofNullable(split.get("type"))
                    .map(mimeService.resolveFromBaseTypeOrInterrogate(tempImageLoc))
                    .orElse("unknown");
            final String topName = split.get("name");

            ImageInfo imageInfo = new ImageInfo(width, height, mimeType, topName);
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
    public String saveImageToTemp(ByteSource findSimpleResource, String sourceName) throws IOException {
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
