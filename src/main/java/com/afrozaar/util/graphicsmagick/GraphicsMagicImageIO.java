package com.afrozaar.util.graphicsmagick;

import static java.lang.String.format;

import com.afrozaar.util.java8.AfrozaarCollectors;

import org.springframework.stereotype.Component;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.ByteSource;

import org.gm4java.engine.GMException;
import org.gm4java.engine.GMService;
import org.gm4java.engine.GMServiceException;
import org.gm4java.engine.support.GMConnectionPoolConfig;
import org.gm4java.engine.support.PooledGMService;
import org.gm4java.im4java.GMBatchCommand;
import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

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
    private GMService service = new PooledGMService(new GMConnectionPoolConfig());

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
    public String resize(final String tempImageLoc, int maximumHeight, int maximumWidth, String newSuffix) throws IOException {
        GMBatchCommand command = new GMBatchCommand(service, COMMAND_CONVERT);

        IMOperation op = new IMOperation();

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

        op.addImage(templateImageLoc);

        op.colorspace("rgb");
        op.crop(size.getX(), size.getY(), offsets.getX(), offsets.getY());

        resizeXY.ifPresent(xy -> {
            op.resize(xy.getX(), xy.getY(), ">");
        });

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

    private String getOutputFileName(String tempImageLoc, String suffix) {
        String tempSuffix = getExtension(tempImageLoc);
        final String name;
        if (tempSuffix != null) {
            name = tempImageLoc.substring(0, tempImageLoc.indexOf(tempSuffix) - 1);
        } else {
            name = tempImageLoc;
        }

        return format("%s_resize%d%s", name, random.nextInt(3), normaliseSuffix0(suffix, tempSuffix));
    }

    private String normaliseSuffix0(String suffix, String tempSuffix) {
        final String suffixOrTempSuffix = Optional.ofNullable(suffix).orElse(tempSuffix);
        final String prependedOrNull = !Strings.isNullOrEmpty(suffixOrTempSuffix) && !suffixOrTempSuffix.startsWith(".") ? "." + suffixOrTempSuffix
                : suffixOrTempSuffix;

        return Optional.ofNullable(prependedOrNull).orElse("").toLowerCase();
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
            //gm identify -format "%w\n%h\n%m\n%t\n" 44284001.JPG
            LOG.debug("identify on {}", tempImageLoc);
            String execute = service.execute(COMMAND_IDENTIFY, "-format", "width=%w\\nheight=%h\\ntype=%m\\nname=%t\\n", tempImageLoc);
            LOG.debug("executed identify {} and got {}", tempImageLoc, execute);

            Map<String, String> split = Arrays.stream(execute.split("\n")).filter(x -> x.contains("=")).map(x -> {
                String[] keyValue = x.split("=");
                return new AbstractMap.SimpleEntry<>(keyValue[0], keyValue[1]);
            }).collect(AfrozaarCollectors.toMap());

            int width = Integer.parseInt(split.get("width"));
            int height = Integer.parseInt(split.get("height"));

            String type = split.get("type");
            String mimeType = "unknown";
            if (type != null) {
                if ("ps".equals(type.toLowerCase())) {
                    mimeType = "application/postscript";
                } else {
                    mimeType = "image/" + type.toLowerCase();
                }
            }
            String topName = split.get("name");

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
        final String sourceNameToUse = Optional.ofNullable(sourceName).orElse(getRandomAlpha(10));

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
        new File(downloadResource).delete();
    }

}
