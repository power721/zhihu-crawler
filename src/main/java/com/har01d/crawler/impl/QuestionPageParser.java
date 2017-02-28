package com.har01d.crawler.impl;

import com.har01d.crawler.Parser;
import com.har01d.crawler.bean.HttpConfig;
import com.har01d.crawler.bean.ImageInfo;
import com.har01d.crawler.bean.ParseResult;
import com.har01d.crawler.service.ZhihuService;
import com.har01d.crawler.util.HttpUtils;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

public class QuestionPageParser implements Parser {

    private static final Logger LOGGER = LogManager.getLogger(QuestionPageParser.class);
    private static final int MIN_VOTE = 30;

    @Autowired
    private ZhihuService service;

    @Autowired
    private HttpConfig httpConfig;

    @Autowired
    private LinkedBlockingQueue<ImageInfo> queue;

    @Value("${question.parser.sleep.time}")
    private long sleep;

    @Value("${base.url}")
    private String zhihuURL;

    @Override
    public ParseResult parse(String url) throws IOException, InterruptedException {
        int offset = 0;
        int pageSize = 50;
        boolean isFirstParse = service.getPageAccessTime(url) != 0;
        String html = HttpUtils.getHtml(url, httpConfig);
        Document doc = Jsoup.parse(html);

        getImages(url, doc);
        service.updatePageAccessTime(url);

        while (true) {
            offset += pageSize;
            if (!isFirstParse && offset >= 50) {
                break;
            }

            Map<String, String> data = new HashMap<>();
            data.put("method", "next");
            data.put("params",
                "{\"url_token\":" + getUrlToken(url) + ",\"pagesize\":" + pageSize + ",\"offset\":" + offset + "}");
            LOGGER.info("url: {}, pageSize: {}, offset: {}", url, pageSize, offset);
            String json = HttpUtils.post(zhihuURL + "/node/QuestionAnswerListV2", data, httpConfig);
            //            LOGGER.debug(json);

            JSONParser parser = new JSONParser();
            JSONObject jsonObject;

            try {
                jsonObject = (JSONObject) parser.parse(json);
            } catch (ParseException e) {
                throw new IOException("parse JSON failed", e);
            }

            JSONArray items = (JSONArray) jsonObject.get("msg");
            if (items.isEmpty()) {
                break;
            }

            int maxVote = 0;
            for (Object item : items) {
                html = (String) item;
                doc = Jsoup.parse(html);
                int vote = getImages(url, doc);
                maxVote = Math.max(maxVote, vote);
            }
            if (maxVote < MIN_VOTE) {
                break;
            }
            Thread.sleep(sleep);
        }
        service.updatePageAccessTime(url);
        return null;
    }

    private int getImages(String url, Document doc) throws InterruptedException {
        Elements votes = doc.select("div.zm-item-vote-info");
        int maxVote = 0;
        for (Element element : votes) {
            int voteCount = Integer.valueOf(element.attr("data-votecount"));
            maxVote = Math.max(maxVote, voteCount);
            if (voteCount >= MIN_VOTE) {
                Element answer = element.parent().parent();
                int answerID = Integer.valueOf(answer.attr("data-aid"));
                Elements images = answer.select("div.zm-item-rich-text img");
                LOGGER.info("{}/answer/{} vote count: {} images: {}", url, answerID, voteCount, images.size());
                handleImages(url, images);
            }
        }
        return maxVote;
    }

    private void handleImages(String url, Elements images) throws InterruptedException {
        for (Element image : images) {
            String imageUrl = image.attr("src");
            LOGGER.debug("original image url: {}", imageUrl);
            if (isValidImage(imageUrl)) {
                ImageInfo imageInfo = service.getImageInfo(imageUrl);
                if (imageInfo == null) {
                    LOGGER.info("add image url: {}", imageUrl);
                    imageInfo = new ImageInfo(imageUrl, url);
                    queue.put(imageInfo);
                }
            }
        }
    }

    private boolean isValidImage(String imageUrl) {
        return imageUrl != null && !imageUrl.isEmpty() && imageUrl.contains("zhimg.com") && imageUrl.contains("_b.");
    }

    private String getUrlToken(String url) {
        return url.substring((zhihuURL + "/question/").length(), url.length());
    }

}
