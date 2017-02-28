package com.har01d.crawler.impl;

import com.har01d.crawler.Downloader;
import com.har01d.crawler.bean.HttpConfig;
import com.har01d.crawler.exception.ServerSideException;
import com.har01d.crawler.service.ZhihuService;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;


public class ImageDownloader implements Downloader {

    private static final Logger LOGGER = LogManager.getLogger(ImageDownloader.class);

    @Autowired
    private ZhihuService service;

    @Autowired
    private HttpConfig httpConfig;

    private int counter;

    @Override
    public boolean download(File imageDirectory, final String imageUrl) throws IOException {
        String[] components = imageUrl.split("/");
        final String fileName = components[components.length - 1];
        final File file = new File(imageDirectory, fileName);

        //        if (ImageUtil.isValidImage(file)) {
        //            LOGGER.info("file {} already complete.", file);
        //            return false;
        //        }

        String userAgent = httpConfig.getUserAgent();
        final RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(httpConfig.getConnectTimeout())
            .setConnectionRequestTimeout(httpConfig.getConnectionRequestTimeout())
            .setCookieSpec(CookieSpecs.IGNORE_COOKIES)
            .setSocketTimeout(httpConfig.getSocketTimeout()).build();

        List<Header> headers =
            httpConfig.getHeaders().entrySet().stream().map(entry -> new BasicHeader(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        CloseableHttpClient httpClient =
            HttpClients.custom().setDefaultRequestConfig(requestConfig).setDefaultHeaders(headers)
                .setUserAgent(userAgent).setConnectionTimeToLive(600L, TimeUnit.SECONDS).build();

        HttpGet httpget = new HttpGet(imageUrl);
        LOGGER.info("Executing download request {}", httpget.getRequestLine());

        // Create a custom response handler
        ResponseHandler<Boolean> responseHandler = response -> {
            int status = response.getStatusLine().getStatusCode();
            if (status >= 200 && status < 300) {
                HttpEntity entity = response.getEntity();
                if (entity == null) {
                    return false;
                } else {
                    FileUtils.copyInputStreamToFile(entity.getContent(), file);
                    long size = entity.getContentLength();
                    if (size != file.length()) {
                        LOGGER.warn("download {} failed, expected size {}, got {}!", imageUrl, size, file.length());
                        return false;
                    }
                    LOGGER.info("download {} completed, file size {}, total download {} images.", imageUrl,
                        file.length(), counter);
                    service.insertImage(imageUrl, file.getAbsolutePath(), size);
                    counter++;
                    return true;
                }
            } else if (status >= 500 && status <= 599) {
                throw new ServerSideException("Unexpected response status: " + status);
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
        };

        try {
            return httpClient.execute(httpget, responseHandler);
        } catch (ServerSideException e) {
            LOGGER.warn("Download {} failed: {}, retrying...", imageUrl, e.getMessage());
            return httpClient.execute(httpget, responseHandler);
        } finally {
            IOUtils.closeQuietly(httpClient);
        }
    }

}
