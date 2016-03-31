package com.afrozaar.util.graphicsmagick.exiftool;

import static com.afrozaar.util.graphicsmagick.exiftool.Profile.Builder.aProfile;

public class Profiles {

    Profile XMP = aProfile("XMP")
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

    Profile IPTC = aProfile("IPTC")
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

    Profile EXIF = aProfile("EXIF")
            .withTag(SupportedTag.Description, "ImageDescription")
            .withTag(SupportedTag.Creator, "Artist")
            .withTag(SupportedTag.Rights, "Copyright")
            .build();

    Profile File = aProfile("File")
            .withTag(SupportedTag.Description, "Comment")
            .withTag(SupportedTag.Creator, "Artist")
            .withTag(SupportedTag.Rights, "Copyright")
            .build();
}
