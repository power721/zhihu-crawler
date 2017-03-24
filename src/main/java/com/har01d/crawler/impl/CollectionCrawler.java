package com.har01d.crawler.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.har01d.crawler.Crawler;
import com.har01d.crawler.Parser;
import com.har01d.crawler.bean.ParseResult;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CollectionCrawler implements Crawler {

    private static final Logger LOGGER = LogManager.getLogger(CollectionCrawler.class);

    @Autowired
    private Parser collectionParser;

    @Autowired
    private Parser questionParser;

    @Value("${urls}")
    private List<String> baseUrls;

    @Value("${collection.crawler.sleep.time}")
    private long sleep;

    @Value("${base.url}")
    private String zhihuURL;

    @Value("${page.parser.thread.size}")
    private int parserThread;

    @Value("${collection.crawler.thread.size}")
    private int crawlerThreads = 1;

    private ExecutorService questionThreadPool;

    private Cache<String, Object> cache;

    @Override
    public void run() {
        cache = CacheBuilder.newBuilder().maximumSize(1000).expireAfterWrite(30, TimeUnit.MINUTES).build();
        questionThreadPool = Executors.newFixedThreadPool(parserThread, new MyThreadFactory("question"));
        ExecutorService crawlerThreadPool = Executors
            .newFixedThreadPool(crawlerThreads, new MyThreadFactory("crawler"));

        for (String baseUrl : baseUrls) {
            if (baseUrl.startsWith(zhihuURL + "/collection/")) {
                crawlerThreadPool.submit(() -> {
                    try {
                        crawler(baseUrl);
                    } catch (InterruptedException e) {
                        LOGGER.error("crawler interrupted.", e);
                    } catch (Throwable e) {
                        LOGGER.error("crawler failed!", e);
                    }
                });
                LOGGER.info("crawler for {} completed.", baseUrl);
            } else if (baseUrl.startsWith(zhihuURL + "/question/")) {
                try {
                    questionParser.parse(baseUrl);
                } catch (InterruptedException e) {
                    LOGGER.error("crawler interrupted.", e);
                } catch (Throwable e) {
                    LOGGER.error("crawler failed!", e);
                }
            }
        }

        crawlerThreadPool.shutdown();
        try {
            crawlerThreadPool.awaitTermination(3L, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            crawlerThreadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }

        try {
            questionThreadPool.awaitTermination(1L, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            questionThreadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
        questionThreadPool.shutdown();
    }

    @Override
    public void crawler() throws InterruptedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void crawler(String baseUrl) throws InterruptedException {
        String url = baseUrl;
        while (true) {
            try {
                ParseResult result = collectionParser.parse(url);
                LOGGER.debug(result);
                for (String pageUrl : result.getUrls()) {
                    if (cache.getIfPresent(pageUrl) == null) {
                        cache.put(pageUrl, "");
                        questionThreadPool.submit(() -> {
                            try {
                                questionParser.parse(pageUrl);
                            } catch (InterruptedException e) {
                                // ignore
                            } catch (Exception e) {
                                LOGGER.error("get html for url {} failed!", pageUrl, e);
                            }
                        });
                    }
                }

                if (!result.hasNext()) {
                    break;
                }

                url = result.getNextUrl();
                Thread.sleep(sleep);
            } catch (URISyntaxException | IOException e) {
                LOGGER.error("get html {} failed!", url, e);
            }
        }
    }

}
