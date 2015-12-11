package com.har01d.crawler.bean;

public class ImageInfo {

    private String url;
    private String pageUrl;
    private String path;
    private long size;
    private long ctime;
    private long mtime;

    public ImageInfo() {
    }

    public ImageInfo(String url, String pageUrl) {
        this.url = url;
        this.pageUrl = pageUrl;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPageUrl() {
        return pageUrl;
    }

    public void setPageUrl(String pageUrl) {
        this.pageUrl = pageUrl;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getCtime() {
        return ctime;
    }

    public void setCtime(long ctime) {
        this.ctime = ctime;
    }

    public long getMtime() {
        return mtime;
    }

    public void setMtime(long mtime) {
        this.mtime = mtime;
    }

    @Override public String toString() {
        return "ImageInfo{" +
            "url='" + url + '\'' +
            ", path='" + path + '\'' +
            ", size=" + size +
            ", ctime=" + ctime +
            ", mtime=" + mtime +
            '}';
    }
}
