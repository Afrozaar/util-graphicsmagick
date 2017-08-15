package com.afrozaar.util.graphicsmagick;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import org.junit.Test;

import java.io.IOException;

/**
 * @author johan
 */
public class GraphicsMagickImageIOTest {

    @Test
    public void resizePdf() throws IOException {
        GraphicsMagickImageIO gmio = new GraphicsMagickImageIO();

        Resource resource = new ClassPathResource("/bin/pdf/Business.pdf");

        String tmpLoc = resource.getFile().getAbsolutePath();

        System.out.println("tmpLoc = " + tmpLoc);

        final String png = gmio.resize(tmpLoc, 1000, 1000, "png");

        System.out.println("png = " + png);
    }

}