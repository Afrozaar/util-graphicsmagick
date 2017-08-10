package com.afrozaar.util.graphicsmagick.meta;

import static org.assertj.core.api.Assertions.assertThat;

import static org.junit.Assert.fail;

import com.google.common.io.ByteSource;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

public class MetaParserTest {

    private static final Logger LOG = LoggerFactory.getLogger(MetaParserTest.class);

    @Test
    public void parseResult() throws Exception {
        final String context = "/imageinfo.txt";
        final Optional<Map<String, Object>> stringObjectMap = MetaParser.parseResult(readOutput(context));
        LOG.info("Parsed object map: {}", stringObjectMap);
    }

    @Test
    public void indentResult() {
        int result = MetaParser.indent(0, "this has no indent");

        assertThat(result).isEqualTo(0);

        int result2 = MetaParser.indent(0, "    this has one indent");
        assertThat(result2).isEqualTo(4);
    }

    @Test
    public void parseWeirdIndents() throws IOException {
        final String context = "/rugbymetadata.txt";
        final Optional<Map<String, Object>> result = MetaParser.parseResult(readOutput(context));

        if (result.isPresent()) {
            ObjectMapper mapper = new ObjectMapper();
            mapper.writer(new DefaultPrettyPrinter()).writeValue(System.out, result.get().get("data"));
        } else {
            fail();
        }
    }

    @Test
    public void rogueLineData() {

        final String input = "this or. that";
        final String input2 = "Profile-1";
        final String regex = "(\\w|\\d|\\s|\\-)+";
        LOG.info("'{}'.matches({}): {}", input, regex, input.matches(regex));
        LOG.info("'{}'.matches({}): {}", input2, regex, input2.matches(regex));
    }

    private String readOutput(final String context) throws IOException {
        return new String(new ByteSource() {
            @Override
            public InputStream openStream() throws IOException {
                return MetaParserTest.class.getResourceAsStream(context);
            }
        }.read(), StandardCharsets.UTF_8);
    }
}