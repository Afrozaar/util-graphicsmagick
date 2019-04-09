package com.afrozaar.util.graphicsmagick.operation;

import static java.util.Optional.ofNullable;

import com.afrozaar.util.graphicsmagick.data.ImageInfo;
import com.afrozaar.util.graphicsmagick.util.RuntimeLimits;

import org.im4java.core.IMOperation;

import java.io.IOException;
import java.util.EnumSet;

import javax.annotation.Nullable;

/**
 * @author johan
 */
public class Convert {

    public static String COMMAND = "convert";

    public static IMOperation createImOperation(String tempImageLoc, ImageInfo imageInfo, @Nullable Double imageQuality, EnumSet<Flag> flags)
            throws IOException {
        IMOperation op = new IMOperation();

        if (RuntimeLimits.applyLimits()) {
            op.limit("threads").addRawArgs("2");
        }

        if ("application/pdf".equalsIgnoreCase(imageInfo.getMimeType())) {
            op.density(300);
        }

        if (!flags.contains(Flag.NO_STRIP)) {
            op.strip();
        }

        op.addImage(tempImageLoc);
        op.autoOrient();

        ofNullable(imageQuality).ifPresent(op::quality);

        op.colorspace("rgb");
        return op;
    }
}
