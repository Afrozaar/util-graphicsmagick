package com.afrozaar.util.graphicsmagick;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.ByteSource;

import org.springframework.stereotype.Component;

import org.gm4java.engine.GMException;
import org.gm4java.engine.GMService;
import org.gm4java.engine.GMServiceException;
import org.gm4java.engine.support.GMConnectionPoolConfig;
import org.gm4java.engine.support.PooledGMService;
import org.gm4java.im4java.GMBatchCommand;
import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;

import javax.annotation.Nullable;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class GraphicsMagicImageIO extends AbstractImageIO {

    public GraphicsMagicImageIO() {
        super();
    }

    public GraphicsMagicImageIO(String tempDir) {
        super(tempDir);
    }

    private static final String COMMAND_CONVERT = "convert";
    private static final String COMMAND_IDENTIFY = "identify";
    private static final Map<String, String> MIME_TYPE_MAP = ImmutableMap.<String, String>builder()
            .put("ps", "application/postscript")
            .put("pdf", "application/pdf")
            .put("jpg", "image/jpeg")
            .put("jpeg", "image/jpeg")
            .put("gif", "image/gif")
            .put("tiff", "image/tiff")
            .build();

    private GMService service = new PooledGMService(new GMConnectionPoolConfig());
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
        final GMBatchCommand command = new GMBatchCommand(service, COMMAND_CONVERT);
        final IMOperation op = new IMOperation();

        final ImageInfo imageInfo = getImageInfo(tempImageLoc, false, null);
        if ("application/pdf".equalsIgnoreCase(imageInfo.getMimeType())) {
            op.density(300);
        }

        op.addImage(tempImageLoc);

        op.colorspace("rgb");
        op.resize(maximumWidth, maximumHeight, ">");

        final String outputFileName = getOutputFileName(tempImageLoc, newSuffix);
        op.addImage(outputFileName);

        // execute the operation
        try {
            LOG.debug("running operation {}", op);
            command.run(op);
            LOG.debug("op {} done", op);
            return outputFileName;
        } catch (InterruptedException | IM4JavaException e) {
            throw new IOException(e);
        }
    }

    @Override
    public String crop(String templateImageLoc, XY size, XY offsets, XY resizeXY, String newSuffix) throws IOException {
        return crop(templateImageLoc, size, offsets, Optional.of(resizeXY), newSuffix);
    }

    public String crop(String templateImageLoc, XY size, XY offsets, Optional<XY> resizeXY, String newSuffix) throws IOException {
        GMBatchCommand command = new GMBatchCommand(service, COMMAND_CONVERT);

        IMOperation op = new IMOperation();

        final ImageInfo imageInfo = getImageInfo(templateImageLoc, false, null);
        if ("application/pdf".equalsIgnoreCase(imageInfo.getMimeType())) {
            op.density(300);
        }

        op.addImage(templateImageLoc);

        op.colorspace("rgb");
        op.crop(size.getX(), size.getY(), offsets.getX(), offsets.getY());

        resizeXY.ifPresent(xy -> op.resize(xy.getX(), xy.getY(), ">"));

        String outputFileName = getOutputFileName(templateImageLoc, newSuffix);
        op.addImage(outputFileName);

        // execute the operation
        try {
            LOG.debug("running operation {}", op);
            command.run(op);
            LOG.debug("op {} done", op);
            return outputFileName;
        } catch (InterruptedException | IM4JavaException e) {
            throw new GraphicsMagickException(outputFileName, e.getMessage(), e);
        }
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

    /**
     * Function producer that takes a resourceUri to fall back to when a type can not be resolved from {@link #MIME_TYPE_MAP}.
     * The produced function takes a type:String input to retrieve from the {@link #MIME_TYPE_MAP}.
     */
    private final Function<String, Function<String, String>> MIME_TYPE_RESOLVER = resourceUri -> type -> ofNullable(MIME_TYPE_MAP.get(type.toLowerCase()))
            .orElseGet(() -> {
                try {
                    return mimeService.getMimeType(resourceUri);
                } catch (IOException e) {
                    return format("image/%s", type.toLowerCase());
                }
            });

    @Override
    public ImageInfo getImageInfo(final String tempImageLoc, boolean includeMeta, MetaDataFormat format) throws IOException {
        try {
            //gm identify -format "%w\n%h\n%m\n%t\n" 44284001.JPG
            LOG.debug("identify on {}", tempImageLoc);
            String execute = service.execute(COMMAND_IDENTIFY, "-format", "width=%w\\nheight=%h\\ntype=%m\\nname=%t\\n", tempImageLoc);
            LOG.debug("executed identify {} and got {}", tempImageLoc, execute);

            String[] split2 = execute.split("\n");
            LOG.debug("result from gm: {}", Arrays.asList(split2));
            Map<String, String> split = Arrays.stream(split2).filter(x -> x.contains("=")).map(x -> {
                String[] keyValue = x.split("=");
                return new AbstractMap.SimpleEntry<>(keyValue[0], keyValue[1]);
            }).collect(Collectors.toMap(Entry::getKey, Entry::getValue, (a, b) -> a)); // when calling info on gifs we get width and height per image, all the widths and heights are going to be the same

            final int width = Integer.parseInt(split.get("width"));
            final int height = Integer.parseInt(split.get("height"));

            final String mimeType = ofNullable(split.get("type")).map(MIME_TYPE_RESOLVER.apply(tempImageLoc)).orElse("unknown");
            final String topName = split.get("name");

            ImageInfo imageInfo = new ImageInfo(width, height, mimeType, topName);
            if (includeMeta) {
                getImageMetaData(tempImageLoc, format).ifPresent(imageInfo::setMetaData);
            }

            LOG.debug("returning image info {} for url {}", imageInfo, tempImageLoc);
            return imageInfo;
        } catch (GMException | GMServiceException e) {
            throw new GraphicsMagickException(null, e.getMessage(), e);
        }
    }

    private Optional<Map<String, Object>> getImageMetaData(final String tempImageLoc, MetaDataFormat format) {
        // gm identify -verbose ~/Pictures/nikon/nikon_20160214/darktable_exported/img_0001.jpg
        try {
            final String execute = service.execute(COMMAND_IDENTIFY, "-verbose", tempImageLoc);

            if (format == MetaDataFormat.RAW) {
                return Optional.of(ImmutableMap.of("data", execute));
            }

            return MetaParser.parseResult(execute);
        } catch (IOException | GMException | GMServiceException e) {
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
