package com.har01d.crawler.bean;

import com.har01d.crawler.Downloader;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.LinkedBlockingQueue;

public class Config {

    @Autowired
    private Downloader downloader;

    @Autowired
    private LinkedBlockingQueue<ImageInfo> queue;

    private String imageDirectory;

    private int poolSize;

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

}
