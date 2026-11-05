package org.originsd.lexibangla.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Representation of a GitHub Release from GitHub REST API.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReleaseInfo {

    @JsonProperty("tag_name")
    private String tagName;

    @JsonProperty("name")
    private String name;

    @JsonProperty("body")
    private String body;

    @JsonProperty("prerelease")
    private boolean prerelease;

    @JsonProperty("draft")
    private boolean draft;

    @JsonProperty("published_at")
    private String publishedAt;

    @JsonProperty("html_url")
    private String htmlUrl;

    @JsonProperty("assets")
    private List<ReleaseAsset> assets;

    public ReleaseInfo() {}

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public boolean isPrerelease() {
        return prerelease;
    }

    public void setPrerelease(boolean prerelease) {
        this.prerelease = prerelease;
    }

    public boolean isDraft() {
        return draft;
    }

    public void setDraft(boolean draft) {
        this.draft = draft;
    }

    public String getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(String publishedAt) {
        this.publishedAt = publishedAt;
    }

    public String getHtmlUrl() {
        return htmlUrl;
    }

    public void setHtmlUrl(String htmlUrl) {
        this.htmlUrl = htmlUrl;
    }

    public List<ReleaseAsset> getAssets() {
        return assets;
    }

    public void setAssets(List<ReleaseAsset> assets) {
        this.assets = assets;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ReleaseAsset {
        @JsonProperty("name")
        private String name;

        @JsonProperty("browser_download_url")
        private String browserDownloadUrl;

        @JsonProperty("size")
        private long size;

        public ReleaseAsset() {}

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getBrowserDownloadUrl() {
            return browserDownloadUrl;
        }

        public void setBrowserDownloadUrl(String browserDownloadUrl) {
            this.browserDownloadUrl = browserDownloadUrl;
        }

        public long getSize() {
            return size;
        }

        public void setSize(long size) {
            this.size = size;
        }
    }
}
