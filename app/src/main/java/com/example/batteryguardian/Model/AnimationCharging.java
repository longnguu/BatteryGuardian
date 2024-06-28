package com.example.batteryguardian.Model;

import android.net.Uri;

public class AnimationCharging {
    private int id;
    private int type;
    private int src;
    private Uri srcUri;

    public void setId(int id) {
        this.id = id;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setSrc(int src) {
        this.src = src;
    }

    public void setSrcUri(Uri srcUri) {
        this.srcUri = srcUri;
    }

    public static class Builder {
        private int id;
        private int type;
        private int src;
        private Uri srcUri;

        public Builder id(int id) {
            this.id = id;
            return this;
        }

        public Builder type(int type) {
            this.type = type;
            return this;
        }

        public Builder src(int src) {
            this.src = src;
            return this;
        }

        public Builder srcUri(Uri srcUri) {
            this.srcUri = srcUri;
            return this;
        }

        public AnimationCharging build() {
            return new AnimationCharging(this);
        }
    }

    private AnimationCharging(Builder builder) {
        this.id = builder.id;
        this.type = builder.type;
        this.src = builder.src;
        this.srcUri = builder.srcUri;
    }

    // Getters for fields
    public int getId() {
        return id;
    }

    public int getType() {
        return type;
    }

    public int getSrc() {
        return src;
    }

    public Uri getSrcUri() {
        return srcUri;
    }
}

