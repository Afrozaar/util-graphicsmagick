package com.afrozaar.util.graphicsmagick;

import static java.lang.String.format;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class GraphicsMagicImageIO extends AbstractImageIO {

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
    }

    @Override
    public String resize(final String tempImageLoc, int maximumHeight, int maximumWidth, String newSuffix) throws IOException {
        GMBatchCommand command = new GMBatchCommand(service, COMMAND_CONVERT);

        IMOperation op = new IMOperation();

        op.addImage(tempImageLoc);

        op.colorspace("rgb");
        op.resize(maximumWidth, maximumHeight);

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
        GMBatchCommand command = new GMBatchCommand(service, COMMAND_CONVERT);

        IMOperation op = new IMOperation();

        op.addImage(templateImageLoc);

        op.colorspace("rgb");
        op.crop(size.getX(), size.getY(), offsets.getX(), offsets.getY());
        if (resizeXY != null) {
            op.resize(resizeXY.getX(), resizeXY.getY());
        }

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

        if (suffix == null) {
            suffix = tempSuffix;
        }
        if (!Strings.isNullOrEmpty(suffix) && !suffix.startsWith(".")) {
            suffix = "." + suffix;
        }

        return format("%s_resize%d%s", name, random.nextInt(3), (suffix == null ? "" : suffix));
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
    public ImageInfo getImageInfo(final String tempImageLoc, boolean includeMeta) throws IOException {
        try {
            //gm identify -format "%w\n%h\n%m\n%t\n" 44284001.JPG
            LOG.debug("identify on {}", tempImageLoc);
            String execute = service.execute(COMMAND_IDENTIFY, "-format", "%w\\n%h\\n%m\\n%t\\n", tempImageLoc);
            LOG.debug("executed identify {} and got {}", tempImageLoc, execute);

            List<String> split = Splitter.on('\n').trimResults().splitToList(execute);
            int width = Integer.parseInt(split.get(0));
            int height = Integer.parseInt(split.get(1));

            String group = split.get(2);
            String mimeType = "unknown";
            if (group != null) {
                if ("ps".equals(group.toLowerCase())) {
                    mimeType = "application/postscript";
                } else {
                    mimeType = "image/" + group.toLowerCase();
                }
            }
            String topName = split.get(3);

            ImageInfo imageInfo = new ImageInfo(width, height, mimeType, topName);
            if (includeMeta) {
                getImageMetaData(tempImageLoc).ifPresent(imageInfo::setMetaData);
            }

            LOG.debug("returning image info {} for url {}", imageInfo, tempImageLoc);
            return imageInfo;
        } catch (GMException | GMServiceException e) {
            throw new GraphicsMagickException(null, e.getMessage(), e);
        }
    }

    private Optional<Map<String, Object>> getImageMetaData(final String tempImageLoc) {
        // gm identify -verbose ~/Pictures/nikon/nikon_20160214/darktable_exported/img_0001.jpg
        try {
            final String execute = service.execute(COMMAND_IDENTIFY, "-verbose", tempImageLoc);

            return MetaParser.parseResult(execute);
        } catch (IOException | GMException | GMServiceException e) {
            LOG.error("Error ", e);
            return Optional.empty();
        }
    }

    @Override
    public String saveImageToTemp(ByteSource findSimpleResource, String sourceName) throws IOException {
        FileOutputStream output = null;
        if (sourceName == null) {
            sourceName = getRandomAlpha(10);
        }
        try {
            File file = new File(getTempFileName(sourceName));
            output = new FileOutputStream(file);
            findSimpleResource.copyTo(output);
            return file.getAbsolutePath();
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
