package com.har01d.crawler.util;

import com.har01d.crawler.bean.HttpConfig;
import com.har01d.crawler.exception.ServerSideException;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public final class HttpUtils {

    private static final Logger LOGGER = LogManager.getLogger(HttpUtils.class);

    public static String post(String url, Map<String, String> data, HttpConfig config) throws IOException {
        String userAgent = config.getUserAgent();
        LOGGER.debug(userAgent);
        final RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(config.getConnectTimeout())
            .setConnectionRequestTimeout(config.getConnectionRequestTimeout())
            .setSocketTimeout(config.getSocketTimeout()).build();

        List<Header> headers =
            config.getHeaders().entrySet().stream().map(entry -> new BasicHeader(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        CloseableHttpClient httpClient =
            HttpClients.custom().setDefaultRequestConfig(requestConfig).setDefaultHeaders(headers)
                .setUserAgent(userAgent).build();

        HttpPost httpPost = new HttpPost(url);
        List<NameValuePair> urlParameters =
            data.entrySet().stream().map(entry -> new BasicNameValuePair(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
        httpPost.setEntity(new UrlEncodedFormEntity(urlParameters));

        LOGGER.info("Executing request {}", httpPost.getRequestLine());

        // Create a custom response handler
        ResponseHandler<String> responseHandler = new MyResponseHandler();

        try {
            return httpClient.execute(httpPost, responseHandler);
        } catch (ServerSideException e) {
            LOGGER.warn("Parse {} failed: {}, retrying...", url, e.getMessage());
            return httpClient.execute(httpPost, responseHandler);
        } finally {
            IOUtils.closeQuietly(httpClient);
        }
    }

    public static String getHtml(String url, HttpConfig config) throws IOException {
        String userAgent = config.getUserAgent();
        LOGGER.debug(userAgent);
        final RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(config.getConnectTimeout())
            .setConnectionRequestTimeout(config.getConnectionRequestTimeout())
            .setSocketTimeout(config.getSocketTimeout()).build();

        List<Header> headers =
            config.getHeaders().entrySet().stream().map(entry -> new BasicHeader(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        CloseableHttpClient httpClient =
            HttpClients.custom().setDefaultRequestConfig(requestConfig).setDefaultHeaders(headers)
                .setUserAgent(userAgent).build();
        HttpGet httpget = new HttpGet(url);

        LOGGER.info("Executing request {}", httpget.getRequestLine());

        // Create a custom response handler
        ResponseHandler<String> responseHandler = new MyResponseHandler();

        try {
            return httpClient.execute(httpget, responseHandler);
        } catch (ServerSideException e) {
            LOGGER.warn("Parse {} failed: {}, retrying...", url, e.getMessage());
            return httpClient.execute(httpget, responseHandler);
        } finally {
            IOUtils.closeQuietly(httpClient);
        }
    }

    public static String normalizeUrl(String baseUrl, String url) throws MalformedURLException {
        URL parent = new URL(baseUrl);
        if (url.startsWith("?")) {
            return parent.toExternalForm().replace("?" + parent.getQuery(), "") + url;
        }

        URL spec = new URL(parent, url);
        return spec.toExternalForm();
    }

    private static class MyResponseHandler implements ResponseHandler<String> {
        @Override
        public String handleResponse(HttpResponse response) throws IOException {
            int status = response.getStatusLine().getStatusCode();
            if (status >= 200 && status < 300) {
                HttpEntity entity = response.getEntity();
                return entity != null ? EntityUtils.toString(entity) : null;
            } else if (status >= 500 && status <= 599) {
                throw new ServerSideException("Unexpected response status: " + status);
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
        }
    }

}
