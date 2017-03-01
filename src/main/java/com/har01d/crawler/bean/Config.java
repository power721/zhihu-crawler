package com.har01d.crawler.bean;

import com.har01d.crawler.Downloader;
import java.util.concurrent.LinkedBlockingQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

public class Config {

    @Autowired
    private Downloader downloader;

    @Autowired
    private LinkedBlockingQueue<ImageInfo> queue;

    @Value("${collection.crawler.thread.size}")
    private int crawlerThreads = 1;

    private String imageDirectory = ".";

    private int poolSize = 10;

    public Downloader getDownloader() {
        return downloader;
    }

    public void setDownloader(Downloader downloader) {
        this.downloader = downloader;
    }

    public String getImageDirectory() {
        return imageDirectory;
    }

    public void setImageDirectory(String imageDirectory) {
        this.imageDirectory = imageDirectory;
    }

    public LinkedBlockingQueue<ImageInfo> getQueue() {
        return queue;
    }

    public void setQueue(LinkedBlockingQueue<ImageInfo> queue) {
        this.queue = queue;
    }

    public int getPoolSize() {
        return poolSize;
    }

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

    public int getCrawlerThreads() {
        return crawlerThreads;
    }

    public void setCrawlerThreads(int crawlerThreads) {
        this.crawlerThreads = crawlerThreads;
    }
}
