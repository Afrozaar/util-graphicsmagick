package com.afrozaar.util.graphicsmagick.operation;

import static java.util.Optional.ofNullable;

import com.afrozaar.util.graphicsmagick.data.ImageInfo;
import com.afrozaar.util.graphicsmagick.util.RuntimeLimits;

import org.im4java.core.IMOperation;

import javax.annotation.Nullable;

import java.io.IOException;

/**
 * @author johan
 */
public class Convert {

    public static String COMMAND = "convert";

    public static IMOperation createImOperation(String tempImageLoc, ImageInfo imageInfo, @Nullable Double imageQuality) throws IOException {
        IMOperation op = new IMOperation();

        if (RuntimeLimits.applyLimits()) {
            op.limit("threads").addRawArgs("2");
        }

        if ("application/pdf".equalsIgnoreCase(imageInfo.getMimeType())) {
            op.density(300);
        }

        op.strip();
        op.addImage(tempImageLoc);
        op.autoOrient();

        ofNullable(imageQuality).ifPresent(op::quality);

        op.colorspace("rgb");
        return op;
    }
}
