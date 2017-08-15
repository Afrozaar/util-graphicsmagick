package com.afrozaar.util.graphicsmagick.operation;

import com.afrozaar.util.graphicsmagick.util.RuntimeLimits;

import org.im4java.core.GMOperation;
import org.im4java.core.IM4JavaException;
import org.im4java.core.Operation;

import java.io.IOException;

public class Identify {

    public static final String COMMAND = "identify";
    static final String IDENTIFY_FORMAT = "width=%w\\nheight=%h\\ntype=%m\\nname=%t\\n";

    /**
     * -----
     * >$ identify -format "width=%w\nheight=%h\ntype=%m\nname=%t\n" maxi.jpg
     * >width=1024
     * >height=768
     * >type=JPEG
     * >name=maxi
     * ----
     *
     * NOTE: type is not the full MIME format: &lt;base_type&gt;/&lt;subtype&gt;
     */
    public static Operation identify(String tempImageLoc) throws InterruptedException, IOException, IM4JavaException {
        GMOperation identifyOp = new GMOperation();

        if (RuntimeLimits.applyLimits()) {
            identifyOp.limit("threads").addRawArgs("1");
        }

        identifyOp.addRawArgs("-format", IDENTIFY_FORMAT);
        identifyOp.addImage(tempImageLoc);

        return identifyOp;
    }

    public static Operation identifyVerbose(String tempImageLoc) throws InterruptedException, IOException, IM4JavaException {
        GMOperation identifyOp = new GMOperation();
        identifyOp.verbose();

        if (RuntimeLimits.applyLimits()) {
            identifyOp.limit("threads").addRawArgs("1");
        }

        identifyOp.addImage(tempImageLoc);

        return identifyOp;
    }

}
