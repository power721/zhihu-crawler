package com.har01d.crawler.impl;

import com.har01d.crawler.Parser;
import com.har01d.crawler.bean.HttpConfig;
import com.har01d.crawler.bean.ParseResult;
import com.har01d.crawler.service.ZhihuService;
import com.har01d.crawler.util.HttpUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CollectionPageParser implements Parser {

    private static final Logger LOGGER = LogManager.getLogger(CollectionPageParser.class);
    private static final long FIRST_ACCESS_TIME = 0L;

    @Autowired
    private ZhihuService service;

    @Autowired
    private HttpConfig httpConfig;

    @Autowired
    @Value("${collection.question.href.selector}")
    private String questionSelector;

    @Autowired
    @Value("${collection.next.href.selector}")
    private String nextUrlSelector;

    @Override
    public ParseResult parse(String baseUrl) throws IOException {
        List<String> urls = new ArrayList<>();
        ParseResult result = new ParseResult(urls);
        String html = HttpUtils.getHtml(baseUrl, httpConfig);
        //        LOGGER.debug(html);
        Document doc = Jsoup.parse(html);
        Elements hrefs = doc.select(questionSelector);

        long time = System.currentTimeMillis();
        for (Element href : hrefs) {
            String url = href.attr("href");
            //            LOGGER.debug("get url: {}", url);
            if (url != null) {
                String pageUrl = HttpUtils.normalizeUrl(baseUrl, url);
                long atime = service.getPageAccessTime(pageUrl);
                LOGGER.info("get question url: {}, last access time {}", pageUrl, atime);
                if ((time - atime) >= 24 * 3600 * 1000) {
                    urls.add(pageUrl);
                    if (atime == FIRST_ACCESS_TIME) {
                        service.insertPage(pageUrl, time);
                    }
                }
            }
        }

        Elements pages = doc.select(nextUrlSelector);
        for (Element page : pages) {
            String text = page.text();
            if ("下一页".equals(text)) {
                String url = page.attr("href");
                String nextUrl = HttpUtils.normalizeUrl(baseUrl, url);
                LOGGER.info("get next page url: {}", nextUrl);
                result.setNextUrl(nextUrl);
            }
        }

        return result;
    }

}
