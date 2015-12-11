package com.har01d.crawler;

import com.har01d.crawler.bean.ParseResult;

import java.io.IOException;


public interface Parser {

    ParseResult parse(String url) throws IOException, InterruptedException;

}
