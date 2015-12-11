package com.har01d.crawler.bean;

public class PageInfo {

    private String url;
    private long ctime;
    private long atime;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getCtime() {
        return ctime;
    }

    public void setCtime(long ctime) {
        this.ctime = ctime;
    }

    public long getAtime() {
        return atime;
    }

    public void setAtime(long atime) {
        this.atime = atime;
    }

    @Override public String toString() {
        return "PageInfo{" +
            "url='" + url + '\'' +
            ", ctime=" + ctime +
            ", atime=" + atime +
            '}';
    }
}
