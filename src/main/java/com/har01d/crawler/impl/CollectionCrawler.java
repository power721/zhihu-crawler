package com.har01d.crawler.impl;

import com.har01d.crawler.Crawler;
import com.har01d.crawler.Parser;
import com.har01d.crawler.bean.ParseResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.util.List;

public class CollectionCrawler implements Crawler {

    private static final Logger LOGGER = LogManager.getLogger(CollectionCrawler.class);

    @Autowired private Parser collectionParser;

    @Autowired private Parser questionParser;

    @Autowired @Value("${urls}") private List<String> baseUrls;

    @Autowired @Value("${collection.crawler.sleep.time}") private long sleep;

    @Override public void run() {
        for (String baseUrl : baseUrls) {
            if (baseUrl.startsWith("http://www.zhihu.com/collection/")) {
                try {
                    crawler(baseUrl);
                    LOGGER.info("crawler for {} completed.", baseUrl);
                } catch (InterruptedException e) {
                    LOGGER.error("crawler interrupted.", e);
                } catch (Throwable e) {
                    LOGGER.error("crawler failed!", e);
                }
            } else if (baseUrl.startsWith("http://www.zhihu.com/question/")) {
                try {
                    questionParser.parse(baseUrl);
                } catch (InterruptedException e) {
                    LOGGER.error("crawler interrupted.", e);
                } catch (Throwable e) {
                    LOGGER.error("crawler failed!", e);
                }
            }
        }
    }

    @Override public void crawler() throws InterruptedException {
        throw new UnsupportedOperationException();
    }

    @Override public void crawler(String baseUrl) throws InterruptedException {
        String url = baseUrl;
        while (true) {
            try {
                ParseResult result = collectionParser.parse(url);
                LOGGER.debug(result);
                for (String pageUrl : result.getUrls()) {
                    try {
                        questionParser.parse(pageUrl);
                    } catch (IOException e) {
                        LOGGER.error("get html for url {} failed!", pageUrl, e);
                    }
                }

                if (!result.hasNext()) {
                    break;
                }

                url = result.getNextUrl();
                Thread.sleep(sleep);
            } catch (IOException e) {
                LOGGER.error("get html {} failed!", url, e);
            }
        }
    }

}
