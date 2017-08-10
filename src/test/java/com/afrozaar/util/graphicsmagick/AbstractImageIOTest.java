package com.afrozaar.util.graphicsmagick;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class AbstractImageIOTest {
    AbstractImageIO imageIO = new GraphicsMagickImageIO();

    @Test
    public void testGetExtensionFromSourceWithQueryParameters_MustNotHaveTheQueryParameters() {
        final String source = "https://inm-baobab-test-eu-west-1.s3-eu-west-1.amazonaws.com/private/inm/caa/11554900/model/0/200/CA_NWS_E1_160117_p01.pdf?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20170405T083604Z&X-Amz-SignedHeaders=host&X-Amz-Expires=3599&X-Amz-Credential=AKIAINMXWPFHXVF2RJZQ%2F20170405%2Feu-west-1%2Fs3%2Faws4_request&X-Amz-Signature=c40e0f61c7604a667ab2eea48b9077edf976cafae4e771c176de18f01487778e";

        final String extension = imageIO.getExtension(source);

        Assertions.assertThat(extension).doesNotContain("X-Amz-Algorithm").isEqualTo(".pdf");

    }

}