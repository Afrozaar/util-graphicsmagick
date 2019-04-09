package com.afrozaar.util.graphicsmagick;

import static org.assertj.core.api.Assertions.assertThat;

import com.afrozaar.util.graphicsmagick.GraphicsMagickImageIO.XY;
import com.afrozaar.util.graphicsmagick.data.ImageInfo;
import com.afrozaar.util.graphicsmagick.meta.MetaDataFormat;
import com.afrozaar.util.graphicsmagick.meta.MetaParser;
import com.afrozaar.util.graphicsmagick.meta.MetaParser.Meta;
import com.afrozaar.util.graphicsmagick.operation.Flag;
import com.afrozaar.util.io.ByteSources;

import com.google.common.io.ByteSource;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.EnumSet;

/**
 * @author johan
 */
public class GraphicsMagickImageIOTest {

    private static final String WEBP = "/bin/620x349.webp";
    private static final String JPEG = "/bin/Picture_600x400.jpg";
    GraphicsMagickImageIO gmio = new GraphicsMagickImageIO();

    @Ignore // jenkins doesn't have it installed
    @Test
    public void resizePdf() throws IOException {

        Resource resource = new ClassPathResource("/bin/pdf/Business.pdf");

        String tmpLoc = resource.getFile().getAbsolutePath();

        System.out.println("tmpLoc = " + tmpLoc);

        final String png = gmio.resize(tmpLoc, 1000, 1000, "png");

        System.out.println("png = " + png);
    }

    @Test
    public void resize() throws FileNotFoundException, IOException {
        ByteSource of = ByteSources.of(() -> getClass().getResourceAsStream(JPEG));
        File createTempFile = File.createTempFile("hello", "goodbye");
        of.copyTo(new FileOutputStream(createTempFile));

        String resize = gmio.resize(createTempFile.getAbsolutePath(), 100, 100, "jpeg", 100D, EnumSet.of(Flag.AUTO_CONVERT));
        System.out.println(resize);
        ImageInfo imageInfo = gmio.getImageInfo(resize, false, MetaDataFormat.PARSED);

        assertThat(imageInfo.getMimeType()).isEqualTo("image/jpeg");
    }

    @Test
    public void resizeToWebp() throws FileNotFoundException, IOException {
        ByteSource of = ByteSources.of(() -> getClass().getResourceAsStream(JPEG));
        File createTempFile = File.createTempFile("hello", "goodbye");
        of.copyTo(new FileOutputStream(createTempFile));

        String resize = gmio.resize(createTempFile.getAbsolutePath(), 100, 100, "webp", 100D, EnumSet.of(Flag.AUTO_CONVERT));
        System.out.println(resize);
        ImageInfo imageInfo = gmio.getImageInfo(resize, false, MetaDataFormat.PARSED);

        assertThat(imageInfo.getMimeType()).isEqualTo("image/webp");
    }

    @Test
    public void WhenNoNewSuffixIsGivenLeaveAsIs() throws FileNotFoundException, IOException {
        ByteSource of = ByteSources.of(() -> getClass().getResourceAsStream(JPEG));
        File createTempFile = File.createTempFile("hello", "goodbye");
        of.copyTo(new FileOutputStream(createTempFile));

        String resize = gmio.resize(createTempFile.getAbsolutePath(), 100, 100, null, 100D, EnumSet.of(Flag.AUTO_CONVERT));
        System.out.println(resize);
        ImageInfo imageInfo = gmio.getImageInfo(resize, false, MetaDataFormat.PARSED);

        assertThat(imageInfo.getMimeType()).isEqualTo("image/jpeg");
    }

    @Test
    public void WhenNoNewSuffixIsGivenLeaveAsIsWebp() throws FileNotFoundException, IOException {
        ByteSource of = ByteSources.of(() -> getClass().getResourceAsStream(WEBP));
        File createTempFile = File.createTempFile("hello", "goodbye");
        of.copyTo(new FileOutputStream(createTempFile));

        String resize = gmio.resize(createTempFile.getAbsolutePath(), 100, 100, null, 100D, EnumSet.of(Flag.AUTO_CONVERT));
        System.out.println(resize);
        ImageInfo imageInfo = gmio.getImageInfo(resize, false, MetaDataFormat.PARSED);

        assertThat(imageInfo.getMimeType()).isEqualTo("image/webp");
    }

    @Test
    public void WhenNoNewSuffixIsGivenAndWebNotSupportedFlagConvertToJpeg() throws FileNotFoundException, IOException {
        ByteSource of = ByteSources.of(() -> getClass().getResourceAsStream(WEBP));
        File createTempFile = File.createTempFile("hello", "goodbye");
        of.copyTo(new FileOutputStream(createTempFile));

        String resize = gmio.resize(createTempFile.getAbsolutePath(), 100, 100, null, 100D, EnumSet.of(Flag.AUTO_CONVERT, Flag.WEBP_NOT_SUPPORTED));
        System.out.println(resize);
        ImageInfo imageInfo = gmio.getImageInfo(resize, false, MetaDataFormat.PARSED);

        assertThat(imageInfo.getMimeType()).isEqualTo("image/jpeg");
    }

    @Test
    public void WhenCropNoNewSuffixIsGivenAndWebNotSupportedFlagConvertToJpeg() throws FileNotFoundException, IOException {
        ByteSource of = ByteSources.of(() -> getClass().getResourceAsStream(WEBP));
        File createTempFile = File.createTempFile("hello", "goodbye");
        of.copyTo(new FileOutputStream(createTempFile));

        String resize = gmio.crop(createTempFile.getAbsolutePath(), XY.of(100, 100), XY.of(100, 100), XY.of(100, 100), null, 100D, EnumSet.of(Flag.AUTO_CONVERT,
                                                                                                                                              Flag.WEBP_NOT_SUPPORTED));
        System.out.println(resize);
        ImageInfo imageInfo = gmio.getImageInfo(resize, false, MetaDataFormat.PARSED);

        assertThat(imageInfo.getMimeType()).isEqualTo("image/jpeg");
    }

    @Test
    public void ResizeAndCropWithNoStrip() throws FileNotFoundException, IOException {
        ByteSource of = ByteSources.of(() -> getClass().getResourceAsStream(JPEG));
        File createTempFile = File.createTempFile("hello", "goodbye");
        of.copyTo(new FileOutputStream(createTempFile));

        {
            String resize = gmio.crop(createTempFile.getAbsolutePath(), XY.of(100, 100), XY.of(100, 100), XY.of(100, 100), null, 100D, EnumSet.of(
                    Flag.AUTO_CONVERT, Flag.WEBP_NOT_SUPPORTED, Flag.NO_STRIP));
            System.out.println(resize);
            ImageInfo imageInfo = gmio.getImageInfo(resize, true, MetaDataFormat.PARSED);
            MetaParser.Meta meta = (Meta) imageInfo.getMetaData().get("data");
            assertThat(meta.getChildren().stream().flatMap(x -> x.getChildren().stream()).map(x -> x.getData())).extracting(x -> (String) x.getKey()).contains(
                    "Exposure Program");
        }

        {
            String resize = gmio.crop(createTempFile.getAbsolutePath(), XY.of(100, 100), XY.of(100, 100), XY.of(100, 100), null, 100D, EnumSet.of(
                    Flag.AUTO_CONVERT, Flag.WEBP_NOT_SUPPORTED));
            System.out.println(resize);
            ImageInfo imageInfo = gmio.getImageInfo(resize, true, MetaDataFormat.PARSED);
            MetaParser.Meta meta = (Meta) imageInfo.getMetaData().get("data");
            assertThat(meta.getChildren().stream().flatMap(x -> x.getChildren().stream()).map(x -> x.getData())).extracting(x -> (String) x.getKey())
                    .doesNotContain("Exposure Program");
        }

        {
            String resize = gmio.resize(createTempFile.getAbsolutePath(), 100, 100, null, 100D, EnumSet.of(Flag.AUTO_CONVERT, Flag.WEBP_NOT_SUPPORTED,
                                                                                                           Flag.NO_STRIP));
            System.out.println(resize);
            ImageInfo imageInfo = gmio.getImageInfo(resize, true, MetaDataFormat.PARSED);
            MetaParser.Meta meta = (Meta) imageInfo.getMetaData().get("data");
            assertThat(meta.getChildren().stream().flatMap(x -> x.getChildren().stream()).map(x -> x.getData())).extracting(x -> (String) x.getKey()).contains(
                    "Exposure Program");
        }

        {
            String resize = gmio.resize(createTempFile.getAbsolutePath(), 100, 100, null, 100D, EnumSet.of(Flag.AUTO_CONVERT, Flag.WEBP_NOT_SUPPORTED));
            System.out.println(resize);
            ImageInfo imageInfo = gmio.getImageInfo(resize, true, MetaDataFormat.PARSED);
            MetaParser.Meta meta = (Meta) imageInfo.getMetaData().get("data");
            assertThat(meta.getChildren().stream().flatMap(x -> x.getChildren().stream()).map(x -> x.getData())).extracting(x -> (String) x.getKey())
                    .doesNotContain("Exposure Program");
        }

    }

}