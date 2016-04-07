package com.afrozaar.util.graphicsmagick.exiftool;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import java.util.List;

/**
 * @author johan
 */
public class ProfilesTest {

    @Test
    public void GetMatchingTags_MustReturnExpectedTags() {

        //XMP, IPTC, EXIF, File

        final List<String> descriptionTags = Profiles.getMatchingTags(SupportedTag.Description);
        assertThat(descriptionTags).containsExactly("XMP:Description", "IPTC:Caption-Abstract", "EXIF:ImageDescription", "File:Comment");

        final List<String> titleTags = Profiles.getMatchingTags(SupportedTag.Title);
        assertThat(titleTags).containsExactly("XMP:Title", "IPTC:ObjectName");

        final List<String> creatorTags = Profiles.getMatchingTags(SupportedTag.Creator);
        assertThat(creatorTags).containsExactly("XMP:Creator", "IPTC:By-line", "EXIF:Artist", "File:Creator");

        final List<String> transRefTags = Profiles.getMatchingTags(SupportedTag.TransmissionRef);
        assertThat(transRefTags).containsExactly("XMP:TransmissionReference", "IPTC:OriginalTransmissionReference");

        final List<String> captionWriterTags = Profiles.getMatchingTags(SupportedTag.CaptionWriter);
        assertThat(captionWriterTags).containsExactly("XMP:CaptionWriter", "IPTC:Writer-Editor");

        final List<String> categoryTags = Profiles.getMatchingTags(SupportedTag.Category);
        assertThat(categoryTags).containsExactly("XMP:Category", "IPTC:Category");

        final List<String> urgencyTags = Profiles.getMatchingTags(SupportedTag.Urgency);
        assertThat(urgencyTags).containsExactly("XMP:Urgency", "IPTC:Urgency");

        final List<String> authorPosTags = Profiles.getMatchingTags(SupportedTag.AuthorsPosition);
        assertThat(authorPosTags).containsExactly("XMP:AuthorsPosition", "IPTC:By-lineTitle");

        final List<String> creditTags = Profiles.getMatchingTags(SupportedTag.Credit);
        assertThat(creditTags).containsExactly("XMP:Credit", "IPTC:Credit");

        final List<String> sourceTags = Profiles.getMatchingTags(SupportedTag.Source);
        assertThat(sourceTags).containsExactly("XMP:Source", "IPTC:Source");

        final List<String> supplCatTags = Profiles.getMatchingTags(SupportedTag.SupplementalCategories);
        assertThat(supplCatTags).containsExactly("XMP:SupplementalCategories", "IPTC:SupplementalCategories");

        final List<String> cityTags = Profiles.getMatchingTags(SupportedTag.City);
        assertThat(cityTags).containsExactly("XMP:City", "IPTC:City");

        final List<String> countryTags = Profiles.getMatchingTags(SupportedTag.Country);
        assertThat(countryTags).containsExactly("XMP:Country", "IPTC:Country-PrimaryLocationName");

        final List<String> rightTags = Profiles.getMatchingTags(SupportedTag.Rights);
        assertThat(rightTags).containsExactly("XMP:Rights", "IPTC:CopyrightNotice", "EXIF:Copyright", "File:Copyright");
    }

}