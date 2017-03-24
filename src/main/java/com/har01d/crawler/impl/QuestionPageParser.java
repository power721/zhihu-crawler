package com.har01d.crawler.impl;

import com.har01d.crawler.Parser;
import com.har01d.crawler.bean.HttpConfig;
import com.har01d.crawler.bean.ImageInfo;
import com.har01d.crawler.bean.ParseResult;
import com.har01d.crawler.service.ZhihuService;
import com.har01d.crawler.util.HttpUtils;
import java.io.IOException;
import java.net.URISyntaxException;
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
    public ParseResult parse(String url) throws IOException, InterruptedException, URISyntaxException {
        int offset = 0;
        int pageSize = 20;
        String id = getQuestionId(url);
        boolean isFirstParse = service.getPageAccessTime(url) != 0;

        while (true) {
            Map<String, String> data = new HashMap<>();
            data.put("sort_by", "default");
            data.put("include",
                "data[*].is_normal,is_sticky,collapsed_by,suggest_edit,comment_count,collapsed_counts,reviewing_comments_count,can_comment,content,editable_content,voteup_count,reshipment_settings,comment_permission,mark_infos,created_time,updated_time,relationship.is_authorized,is_author,voting,is_thanked,is_nothelp,upvoted_followees;data[*].author.is_blocking,is_blocked,is_followed,voteup_count,message_thread_token,badge[?(type=best_answerer)].topics");
            data.put("limit", String.valueOf(pageSize));
            data.put("offset", String.valueOf(offset));
            LOGGER.info("url: {}, pageSize: {}, offset: {}", url, pageSize, offset);
            String json = HttpUtils.get(zhihuURL + "/api/v4/questions/" + id + "/answers", data, httpConfig);

            JSONParser parser = new JSONParser();
            JSONObject jsonObject;

            try {
                jsonObject = (JSONObject) parser.parse(json);
            } catch (ParseException e) {
                throw new IOException("parse JSON failed", e);
            }

            JSONArray items = (JSONArray) jsonObject.get("data");
            if (items.isEmpty()) {
                break;
            }

            for (Object item : items) {
                JSONObject object = (JSONObject) item;
                String html = (String) object.get("content");
                if (html == null) {
                    continue;
                }
                Document doc = Jsoup.parse(html);
                getImages(url, doc);
            }

            offset += items.size();
            if (!isFirstParse && offset >= 20) {
                break;
            }
            Thread.sleep(sleep);
        }
        service.updatePageAccessTime(url);
        return null;
    }

    private String getQuestionId(String url) {
        String[] comps = url.split("/");
        for (int i = 0; i < comps.length; ++i) {
            if ("question".equals(comps[i])) {
                if (i + 1 < comps.length) {
                    return comps[i + 1];
                }
            }
        }
        return null;
    }

    private void getImages(String url, Document doc) throws InterruptedException {
        Elements images = doc.select("img");
        handleImages(url, images);
    }

    private void handleImages(String url, Elements images) throws InterruptedException {
        for (Element image : images) {
            String imageUrl = image.attr("src");
            LOGGER.debug("original image url: {}", imageUrl);
            if (isValidImage(imageUrl)) {
                ImageInfo imageInfo = service.getImageInfo(imageUrl);
                if (imageInfo == null) {
                    LOGGER.info("add image url: {} queue size: {}", imageUrl, queue.size());
                    imageInfo = new ImageInfo(imageUrl, url);
                    queue.put(imageInfo);
                }
            }
        }
    }

    private boolean isValidImage(String imageUrl) {
        return imageUrl != null && !imageUrl.isEmpty() && imageUrl.contains("zhimg.com") && imageUrl.contains("_b.");
    }

}
