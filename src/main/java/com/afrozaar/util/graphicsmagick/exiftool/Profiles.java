package com.afrozaar.util.graphicsmagick.exiftool;

import static com.afrozaar.util.graphicsmagick.exiftool.Profile.Builder.aProfile;

import static java.lang.String.format;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Profiles {

    private static final Profile XMP;
    private static final Profile IPTC;
    private static final Profile EXIF;
    private static final Profile File;

    public static List<String> getMatchingTags(SupportedTag supportedTag) {
        return Stream.of(XMP, IPTC, EXIF, File)
                .map(profile -> profile.getTagString(supportedTag))
                .filter(Optional::isPresent).map(Optional::get)
                .collect(Collectors.toList());
    }

    public static List<String> getProfileTagStringsForRequestedTags(Map<SupportedTag, Object> tagMap) {
        return tagMap.entrySet().stream()
                .flatMap(entry -> getMatchingTags(entry.getKey()).stream()
                        .map(profileTag -> format("%s=%s", profileTag, entry.getValue())))
                .collect(Collectors.toList());
    }

    static {
        XMP = aProfile("XMP")
                .withTag(SupportedTag.Description, "Description")
                .withTag(SupportedTag.Title, "Title")
                .withTag(SupportedTag.Creator, "Creator")
                .withTag(SupportedTag.TransmissionRef, "TransmissionReference")
                .withTag(SupportedTag.CaptionWriter, "CaptionWriter")
                .withTag(SupportedTag.Category, "Category")
                .withTag(SupportedTag.Urgency, "Urgency")
                .withTag(SupportedTag.AuthorsPosition, "AuthorsPosition")
                .withTag(SupportedTag.Credit, "Credit")
                .withTag(SupportedTag.Source, "Source")
                .withTag(SupportedTag.SupplementalCategories, "SupplementalCategories")
                .withTag(SupportedTag.City, "City")
                .withTag(SupportedTag.Country, "Country")
                .withTag(SupportedTag.Rights, "Rights")
                .build();

        /**
         * @see <a href="https://www.iptc.org/std/Iptc4xmpCore/1.0/specification/Iptc4xmpCore_1.0-spec-XMPSchema_8.pdf">IPTC Core XMP Schema</a>
         */
        IPTC = aProfile("IPTC")
                .withTag(SupportedTag.Description, "Caption-Abstract") // 2000 bytes
                .withTag(SupportedTag.Title, "ObjectName") // 64 bytes
                .withTag(SupportedTag.Creator, "By-line") // 32 bytes
                .withTag(SupportedTag.TransmissionRef, "OriginalTransmissionReference") // 32 bytes
                .withTag(SupportedTag.CaptionWriter, "Writer-Editor") // 32 bytes
                .withTag(SupportedTag.Category, "Category")
                .withTag(SupportedTag.Urgency, "Urgency")
                .withTag(SupportedTag.AuthorsPosition, "By-lineTitle") // 32 bytes
                .withTag(SupportedTag.Credit, "Credit") // 32 bytes
                .withTag(SupportedTag.Source, "Source") // 32 bytes
                .withTag(SupportedTag.SupplementalCategories, "SupplementalCategories")
                .withTag(SupportedTag.City, "City") // 32 bytes
                .withTag(SupportedTag.Country, "Country-PrimaryLocationName") // 64 bytes
                .withTag(SupportedTag.Rights, "CopyrightNotice") // 128 bytes
                .build();

        EXIF = aProfile("EXIF")
                .withTag(SupportedTag.Description, "ImageDescription")
                .withTag(SupportedTag.Creator, "Artist")
                .withTag(SupportedTag.Rights, "Copyright")
                .build();

        File = aProfile("File")
                .withTag(SupportedTag.Description, "Comment")
                .withTag(SupportedTag.Creator, "Creator")
                .withTag(SupportedTag.Rights, "Copyright")
                .build();
    }
}
