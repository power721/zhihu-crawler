package com.har01d.crawler.impl;

import com.har01d.crawler.Downloader;
import com.har01d.crawler.bean.Config;
import com.har01d.crawler.bean.ImageInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class Worker implements Runnable {

    private static final Logger LOGGER = LogManager.getLogger(Worker.class);

    private final Downloader downloader;

    private final String baseDirectory;

    private final LinkedBlockingQueue<ImageInfo> queue;

    private int counter;
    private boolean isDone;

    public Worker(Config config) {
        this.downloader = config.getDownloader();
        this.baseDirectory = config.getImageDirectory();
        this.queue = config.getQueue();
    }

    @Override
    public void run() {
        try {
            while (true) {
                ImageInfo imageInfo = queue.poll(500L, TimeUnit.MILLISECONDS);
                if (imageInfo == null) {
                    if (isDone) {
                        break;
                    }
                    continue;
                }

                String imageUrl = imageInfo.getUrl();
                String[] components = imageInfo.getPageUrl().split("/");
                String dirName = components[components.length - 1];
                File directory = new File(baseDirectory, dirName);
                LOGGER.debug("try to download {} to directory {}, remaining {} in queue", imageUrl, directory,
                    queue.size());
                try {
                    if (downloader.download(directory, imageUrl)) {
                        counter++;
                    }
                } catch (IOException e) {
                    LOGGER.error("download image {} failed!", imageUrl, e);
                }
            }
        } catch (InterruptedException e) {
            LOGGER.error("download image interrupted!", e);
        } catch (Throwable e) {
            LOGGER.error("exception occurred!", e);
        }

        LOGGER.info("download {} images.", counter);
    }

    public void done() {
        isDone = true;
    }

    public int getCounter() {
        return counter;
    }

}
