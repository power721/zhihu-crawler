package com.har01d.crawler.impl;

import com.har01d.crawler.Parser;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import static org.junit.Assert.*;

public class QuestionPageParserTest {
    @Test
    public void test() throws Exception {
        ApplicationContext context = new ClassPathXmlApplicationContext("app-context.xml");
        Parser parser = context.getBean(QuestionPageParser.class);
        parser.parse("https://www.zhihu.com/question/39833238");
    }
}
