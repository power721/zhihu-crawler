package com.har01d.crawler;

import java.io.File;
import java.io.IOException;

public interface Downloader {
    boolean download(File imageDirectory, String url) throws IOException;
}
