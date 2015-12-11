package com.har01d.crawler.util;

import org.junit.Test;

import static org.junit.Assert.*;

public class HttpUtilsTest {

    @Test
    public void testAbsolutePath() throws Exception {
        assertEquals("http://www.zhihu.com/collection/60771406?page=2", HttpUtils.normalizeUrl("http://www.zhihu.com/collection/60771406", "http://www.zhihu.com/collection/60771406?page=2"));
    }

    @Test
    public void testRelativePath() throws Exception {
        assertEquals("http://www.zhihu.com/collection/60771406?page=2", HttpUtils.normalizeUrl("http://www.zhihu.com/collection/60771406", "?page=2"));
    }

    @Test
    public void testRelativePath2() throws Exception {
        assertEquals("http://www.zhihu.com/collection/60771406?page=3", HttpUtils.normalizeUrl("http://www.zhihu.com/collection/60771406?page=2", "?page=3"));
    }

    @Test
    public void testRelativeContext() throws Exception {
        assertEquals("http://www.zhihu.com/question/26915909", HttpUtils.normalizeUrl("http://www.zhihu.com/collection/60771406?page=2", "/question/26915909"));
    }

}