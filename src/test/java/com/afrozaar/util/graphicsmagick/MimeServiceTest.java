package com.afrozaar.util.graphicsmagick;

import static org.assertj.core.api.Assertions.assertThat;

import com.afrozaar.util.graphicsmagick.mime.MimeService;

import org.springframework.core.io.ClassPathResource;

import org.junit.Test;

public class MimeServiceTest {

    MimeService mimeService = new MimeService();

    @Test
    public void getMimeType() throws Exception {
        assertThat(mimeService.getMimeType(new ClassPathResource("/bin/Picture_600x400.jpg").getFile().getAbsolutePath())).isEqualTo("image/jpeg");
        assertThat(mimeService.getMimeType(new ClassPathResource("/imageinfo.txt").getFile().getAbsolutePath())).isEqualTo("text/plain");
        assertThat(mimeService.getMimeType(new ClassPathResource("/logback-test.xml").getFile().getAbsolutePath())).isEqualTo("text/plain");

        for (int i = 0; i < 100; i++) {
            assertThat(mimeService.getMimeType(new ClassPathResource("/logback-test.xml").getFile().getAbsolutePath())).isEqualTo("text/plain");
        }

    }

    @Test
    public void checkSupportedMimeTypeTest() {
        {
            final String mimeType = mimeService.supportedMimeType("application/xml");

            assertThat(mimeType).isEqualTo("image/png");
        }
        {
            final String mimeType = mimeService.supportedMimeType("image/jpeg");

            assertThat(mimeType).isEqualTo("image/jpeg");
        }
        {
            final String mimeType = mimeService.supportedMimeType("application/pdf");

            assertThat(mimeType).isEqualTo("application/pdf");
        }
    }

}