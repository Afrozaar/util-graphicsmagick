package com.afrozaar.util.graphicsmagick.operation;

import static java.util.Optional.ofNullable;

import com.afrozaar.util.graphicsmagick.data.ImageInfo;
import com.afrozaar.util.graphicsmagick.util.RuntimeLimits;

import org.im4java.core.IMOperation;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

/**
 * @author johan
 */
public class Convert {

    private static final List<Flag> EMPTY_LIST = Collections.emptyList();

    public static String COMMAND = "convert";

    public static IMOperation createImOperation(String tempImageLoc, ImageInfo imageInfo, @Nullable Double imageQuality, Flag... flags0) throws IOException {
        IMOperation op = new IMOperation();

        if (RuntimeLimits.applyLimits()) {
            op.limit("threads").addRawArgs("2");
        }

        if ("application/pdf".equalsIgnoreCase(imageInfo.getMimeType())) {
            op.density(300);
        }

        List<Flag> flags = flags0 == null ? EMPTY_LIST : Arrays.asList(flags0);

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
