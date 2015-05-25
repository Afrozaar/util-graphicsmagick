package com.afrozaar.util.graphicsmagick;

import com.google.common.base.Splitter;
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
import java.util.StringTokenizer;
import java.util.regex.Pattern;

@Component
public class GraphicsMagicImageIO extends AbstractImageIO {

    GMService service = new PooledGMService(new GMConnectionPoolConfig());

    private static final Pattern resolutionPattern = Pattern.compile("\\s(\\d+)x(\\d+)");
    private static final Pattern imageType = Pattern.compile("\\s(\\w+)\\s");

    @Override
    public String resize(String tempImageLoc, int maximumHeight, int maximumWidth, String newSuffix) throws IOException {
        GMBatchCommand command = new GMBatchCommand(service, "convert");

        IMOperation op = new IMOperation();

        op.addImage(tempImageLoc);

        op.colorspace("rgb");
        op.resize(maximumWidth, maximumHeight);

        String outputFileName = getOutputFileName(tempImageLoc, newSuffix);
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

    public String crop(String tempImageLoc, int height, int width, int offsetx, int offsety, String newSuffix) throws IOException {
        GMBatchCommand command = new GMBatchCommand(service, "convert");

        IMOperation op = new IMOperation();

        op.addImage(tempImageLoc);

        op.colorspace("rgb");
        op.crop(width, height, offsetx, offsety);

        String outputFileName = getOutputFileName(tempImageLoc, newSuffix);
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

    private String getOutputFileName(String tempImageLoc, String suffix) {
        String tempSuffix = getExtension(tempImageLoc);
        String name = null;
        if (tempSuffix != null) {
            name = tempImageLoc.substring(0, tempImageLoc.indexOf(tempSuffix) - 1);
        } else {
            name = tempImageLoc;
        }

        if (suffix == null) {
            suffix = tempSuffix;
        }
        if (!suffix.startsWith(".")) {
            suffix = "." + suffix;
        }

        return name + "_resize" + random.nextInt(3) + suffix;
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
    public ImageInfo getImageInfo(String tempImageLoc) throws IOException {

        String execute;
        try {
            //gm identify -format "%w\n%h\n%m\n%t\n" 44284001.JPG
            LOG.debug("identify on {}", tempImageLoc);
            execute = service.execute("identify", "-format", "%w\\n%h\\n%m\\n%t\\n", tempImageLoc);
            LOG.debug("executed identify {} and got {}", tempImageLoc, execute);

            StringTokenizer tokenizer = new StringTokenizer(execute, "\n");
            int width = 0;
            int height = 0;

            List<String> split = Splitter.on('\n').trimResults().splitToList(execute);
            width = Integer.parseInt(split.get(0));
            height = Integer.parseInt(split.get(1));

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
            LOG.debug("returning image info {} for url {}", imageInfo, tempImageLoc);
            return imageInfo;
        } catch (GMException | GMServiceException e) {
            throw new IOException(e);
        }

    }

    @Override
    public String saveImageToTemp(ByteSource findSimpleResource, String sourceName) throws IOException {
        FileOutputStream output = null;
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

    public GMService getService() {
        return service;
    }

}
