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

        IPTC = aProfile("IPTC")
                .withTag(SupportedTag.Description, "Caption-Abstract")
                .withTag(SupportedTag.Title, "ObjectName")
                .withTag(SupportedTag.Creator, "By-line")
                .withTag(SupportedTag.TransmissionRef, "OriginalTransmissionReference")
                .withTag(SupportedTag.CaptionWriter, "Writer-Editor")
                .withTag(SupportedTag.Category, "Category")
                .withTag(SupportedTag.Urgency, "Urgency")
                .withTag(SupportedTag.AuthorsPosition, "By-lineTitle")
                .withTag(SupportedTag.Credit, "Credit")
                .withTag(SupportedTag.Source, "Source")
                .withTag(SupportedTag.SupplementalCategories, "SupplementalCategories")
                .withTag(SupportedTag.City, "City")
                .withTag(SupportedTag.Country, "Country-PrimaryLocationName")
                .withTag(SupportedTag.Rights, "CopyrightNotice")
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
