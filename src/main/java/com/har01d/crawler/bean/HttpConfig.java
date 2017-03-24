package com.har01d.crawler.bean;

import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.annotation.PostConstruct;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.cookie.BasicClientCookie;

public class HttpConfig {

    private static final int CONNECTION_TIMEOUT_MS = 300 * 1000;
    private static final int CONNECTION_REQUEST_TIMEOUT_MS = 300 * 1000;
    private static final int SOCKET_TIMEOUT_MS = 300 * 1000;

    private List<String> userAgents;
    private Map<String, String> headers;
    private int connectTimeout = CONNECTION_TIMEOUT_MS;
    private int connectionRequestTimeout = CONNECTION_REQUEST_TIMEOUT_MS;
    private int socketTimeout = SOCKET_TIMEOUT_MS;
    private String token;
    private BasicCookieStore cookieStore = new BasicCookieStore();

    @PostConstruct
    public void init() {
        BasicClientCookie cookie = new BasicClientCookie("z_c0", token);
        cookie.setDomain("www.zhihu.com");
        cookie.setPath("/");
        cookieStore.addCookie(cookie);
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public List<String> getUserAgents() {
        return userAgents;
    }

    public void setUserAgents(List<String> userAgents) {
        this.userAgents = userAgents;
    }

    public String getUserAgent() {
        Random random = new Random();
        return userAgents.get(random.nextInt(userAgents.size()));
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getConnectionRequestTimeout() {
        return connectionRequestTimeout;
    }

    public void setConnectionRequestTimeout(int connectionRequestTimeout) {
        this.connectionRequestTimeout = connectionRequestTimeout;
    }

    public int getSocketTimeout() {
        return socketTimeout;
    }

    public void setSocketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public BasicCookieStore getCookieStore() {
        return cookieStore;
    }

    @Override
    public String toString() {
        return "HttpConfig{" +
            "userAgents=" + userAgents +
            ", headers=" + headers +
            ", connectTimeout=" + connectTimeout +
            ", connectionRequestTimeout=" + connectionRequestTimeout +
            ", socketTimeout=" + socketTimeout +
            '}';
    }

}
