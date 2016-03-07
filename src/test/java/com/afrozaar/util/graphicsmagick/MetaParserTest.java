package com.afrozaar.util.graphicsmagick;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.io.ByteSource;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

public class MetaParserTest {

    @Test
    public void parseResult() throws Exception {
        ByteSource byteSource = new ByteSource() {
            @Override
            public InputStream openStream() throws IOException {
                return MetaParserTest.class.getResourceAsStream("/imageinfo.txt");
            }
        };

        MetaParser.parseResult(new String(byteSource.read()));
    }

    @Test
    public void indentResult() {
        int result = MetaParser.indent(0, "this has no indent");

        assertThat(result).isEqualTo(0);

        int result2 = MetaParser.indent(0, "    this has one indent");
        assertThat(result2).isEqualTo(4);
    }
}