package com.har01d.crawler;

public interface Crawler extends Runnable {

    void crawler() throws InterruptedException;

    void crawler(String url) throws InterruptedException;
}
