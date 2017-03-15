package com.afrozaar.util.graphicsmagick;

import com.google.common.base.Preconditions;

import javax.validation.constraints.NotNull;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class ImageLoc implements Externalizable {

    public String imageUrl;
    private String publicLocationPrefix;
    public String sourceUrl;

    private int width;
    private int height;
    private String mimeType;
    private boolean error;

    public ImageLoc() {
        super();
    }

    public ImageLoc(@NotNull String imageUrl, @NotNull String publicLocationPrefix, @NotNull String sourceUrl) {
        super();
        Preconditions.checkNotNull(imageUrl, "imageUrl cannot be null");
        Preconditions.checkNotNull(publicLocationPrefix, "publicLocation cannot be null");
        Preconditions.checkNotNull(sourceUrl, "sourceUrl cannot be null");

        this.imageUrl = imageUrl;
        this.publicLocationPrefix = publicLocationPrefix;
        this.sourceUrl = sourceUrl;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        complex(out);
    }

    private void complex(ObjectOutput out) throws IOException {
        out.writeObject(imageUrl);
        out.writeObject(publicLocationPrefix);
        out.writeObject(sourceUrl);
        out.writeObject(mimeType);
        out.writeInt(width);
        out.writeInt(height);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        complex(in);
    }

    private void complex(ObjectInput in) throws IOException {
        try {
            this.imageUrl = (String) in.readObject();
            this.publicLocationPrefix = (String) in.readObject();
            this.sourceUrl = (String) in.readObject();
            this.mimeType = (String) in.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        }
        this.width = in.readInt();
        this.height = in.readInt();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    @Override
    public String toString() {
        return "ImageLoc [imageUrl=" + imageUrl + ", publicLocation=" + getPublicLocation() + ", sourceUrl=" + sourceUrl + ", width=" + width + ", height=" + height
                + ", mimeType=" + mimeType + "]";
    }

    public void setError(boolean error) {
        this.error = true;

    }

    public String getPublicLocation() {
        String separator;
        if (publicLocationPrefix.endsWith("/") && imageUrl.startsWith("/")) {
            return publicLocationPrefix + imageUrl.substring(1);
        } else if (publicLocationPrefix.endsWith("/")) {
            return publicLocationPrefix + imageUrl;
        } else if (imageUrl.startsWith("/")) {
            return publicLocationPrefix + imageUrl;
        } else {
            return publicLocationPrefix + "/" + imageUrl;
        }
    }

}