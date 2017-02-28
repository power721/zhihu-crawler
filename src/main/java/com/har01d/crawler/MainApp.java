package com.har01d.crawler;

import com.har01d.crawler.bean.Config;
import com.har01d.crawler.impl.MyThreadFactory;
import com.har01d.crawler.impl.Worker;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class MainApp {

    private static final Logger LOGGER = LogManager.getLogger(MainApp.class);

    public static void main(String[] args) {
        Calendar startTime = Calendar.getInstance();
        ApplicationContext context = new ClassPathXmlApplicationContext("app-context.xml");

        Config config = context.getBean(Config.class);
        Crawler crawler = context.getBean(Crawler.class);
        ExecutorService crawlerThreadPool = Executors.newSingleThreadExecutor(new MyThreadFactory("crawler"));
        crawlerThreadPool.submit(crawler);

        ExecutorService downloadThreadPool = Executors
            .newFixedThreadPool(config.getPoolSize(), new MyThreadFactory("worker"));
        Worker[] workers = new Worker[config.getPoolSize()];
        for (int i = 0; i < config.getPoolSize(); i++) {
            workers[i] = new Worker(config);
            downloadThreadPool.submit(workers[i]);
        }

        downloadThreadPool.shutdown();
        crawlerThreadPool.shutdown();
        try {
            crawlerThreadPool.awaitTermination(1L, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            crawlerThreadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }

        for (Worker worker : workers) {
            worker.done();
        }

        try {
            downloadThreadPool.awaitTermination(1L, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            downloadThreadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }

        int counter = 0;
        for (Worker worker : workers) {
            counter += worker.getCounter();
        }
        Calendar endTime = Calendar.getInstance();
        LOGGER.info("total download {} images.", counter);
        LOGGER
            .info("crawler completed, start at {}, end at {}, spend time {} ms", startTime.getTime(), endTime.getTime(),
                (endTime.getTimeInMillis() - startTime.getTimeInMillis()));
    }

}
