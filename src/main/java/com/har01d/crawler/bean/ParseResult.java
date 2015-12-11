package com.har01d.crawler.bean;

import java.util.List;

public class ParseResult {

    private String nextUrl;
    private List<String> urls;

    public ParseResult() {
    }

    public ParseResult(List<String> urls) {
        this.urls = urls;
    }

    public ParseResult(List<String> urls, String nextUrl) {
        this.urls = urls;
        this.nextUrl = nextUrl;
    }

    public List<String> getUrls() {
        return urls;
    }

    public void setUrls(List<String> urls) {
        this.urls = urls;
    }

    public boolean hasNext() {
        return nextUrl != null && !nextUrl.isEmpty();
    }

    public String getNextUrl() {
        return nextUrl;
    }

    public void setNextUrl(String nextUrl) {
        this.nextUrl = nextUrl;
    }

    @Override public String toString() {
        return "ParseResult{" +
            "nextUrl='" + nextUrl + '\'' +
            ", urls=" + urls +
            '}';
    }

}
