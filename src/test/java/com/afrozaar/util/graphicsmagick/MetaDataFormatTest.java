package com.afrozaar.util.graphicsmagick;

import static org.assertj.core.api.Assertions.assertThat;

import com.afrozaar.util.test.TestUtil;

import org.junit.Test;

public class MetaDataFormatTest {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MetaDataFormatTest.class);

    @Test
    public void When_Gibberish_Expect_PARSED() {
        assertThat(MetaDataFormat.fromString(TestUtil.randomString(4))).isEqualTo(MetaDataFormat.PARSED);
    }

    @Test
    public void When_LowerCase_Expect_PARSED() {
        assertThat(MetaDataFormat.fromString("parsed")).isEqualTo(MetaDataFormat.PARSED);
    }

    @Test
    public void When_PARSED_Expect_PARSED() {
        assertThat(MetaDataFormat.fromString("PARSED")).isEqualTo(MetaDataFormat.PARSED);
    }

    @Test
    public void When_RAW_Expect_RAW() {
        assertThat(MetaDataFormat.fromString("RAW")).isEqualTo(MetaDataFormat.RAW);
    }

    @Test
    public void When_raw_Expect_RAW() {
        assertThat(MetaDataFormat.fromString("raw")).isEqualTo(MetaDataFormat.RAW);
    }
    
    @Test
    public void When_NULL_Expect_PARSED() {
        assertThat(MetaDataFormat.fromString("raw")).isEqualTo(MetaDataFormat.RAW);
    }


}
