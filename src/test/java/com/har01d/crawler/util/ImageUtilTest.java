package com.har01d.crawler.util;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

public class ImageUtilTest {
    @Test
    public void testValidPng() throws Exception {
        String path = this.getClass().getClassLoader().getResource("./1.png").getPath();
        File file = new File(path);
        assertTrue(ImageUtil.isValidImage(file));
    }

    @Test
    public void testValidJpeg() throws Exception {
        String path = this.getClass().getClassLoader().getResource("./1.jpg").getPath();
        File file = new File(path);
        assertTrue(ImageUtil.isValidImage(file));
    }
}