package com.har01d.crawler.service;

import com.har01d.crawler.bean.ImageInfo;
import com.har01d.crawler.bean.PageInfo;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ZhihuService {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void init(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public PageInfo getPageInfo(String url) {
        List<PageInfo> results =
            this.jdbcTemplate.query("SELECT url,ctime,atime FROM zhihu_pages WHERE url=?", (resultSet, i) -> {
                PageInfo pageInfo = new PageInfo();
                pageInfo.setAtime(resultSet.getLong("atime"));
                pageInfo.setCtime(resultSet.getLong("ctime"));
                pageInfo.setUrl(resultSet.getString("url"));
                return pageInfo;
            }, url);

        if (results.isEmpty()) {
            return null;
        } else {
            return results.get(0);
        }
    }

    public long getPageAccessTime(String url) {
        List<Long> results =
            this.jdbcTemplate.query("SELECT atime FROM zhihu_pages WHERE url=?", (resultSet, i) -> {
                return resultSet.getLong(1);
            }, url);

        if (results.isEmpty()) {
            return 0;
        } else {
            return results.get(0);
        }
    }

    public int insertPage(String url, long time) {
        return this.jdbcTemplate.update("INSERT INTO zhihu_pages (url, ctime) VALUES (?, ?)", url, time);
    }

    public int updatePageAccessTime(String url) {
        long atime = System.currentTimeMillis();
        return this.jdbcTemplate.update("UPDATE zhihu_pages SET atime=? WHERE url=?", atime, url);
    }

    public ImageInfo getImageInfo(String url) {
        List<ImageInfo> results = this.jdbcTemplate
            .query("SELECT url,size,path,ctime,mtime FROM zhihu_images WHERE url=?", (resultSet, i) -> {
                ImageInfo imageInfo = new ImageInfo();
                imageInfo.setMtime(resultSet.getLong("mtime"));
                imageInfo.setCtime(resultSet.getLong("ctime"));
                imageInfo.setUrl(resultSet.getString("url"));
                imageInfo.setPath(resultSet.getString("path"));
                imageInfo.setSize(resultSet.getLong("size"));
                return imageInfo;
            }, getImageNameFromURL(url));

        if (results.isEmpty()) {
            return null;
        } else {
            return results.get(0);
        }
    }

    public long getImageModifyTime(String url) {
        List<Long> results =
            this.jdbcTemplate.query("SELECT atime FROM zhihu_images WHERE url=?", (resultSet, i) -> {
                return resultSet.getLong(1);
            }, getImageNameFromURL(url));

        if (results.isEmpty()) {
            return 0;
        } else {
            return results.get(0);
        }
    }

    public int insertImage(String url, long size) {
        long ctime = System.currentTimeMillis();
        return this.jdbcTemplate.update("INSERT INTO zhihu_images (url, size, ctime) VALUES (?, ?)", getImageNameFromURL(url), size, ctime);
    }

    public int insertImage(String url, String path, long size) {
        long ctime = System.currentTimeMillis();
        return this.jdbcTemplate
            .update("INSERT INTO zhihu_images (url, path, size, ctime) VALUES (?, ?, ?, ?)", getImageNameFromURL(url), path, size, ctime);
    }

    public int updateImage(String url, String path) {
        long mtime = System.currentTimeMillis();
        return this.jdbcTemplate.update("UPDATE zhihu_images SET mtime=?,path=? WHERE url=?", mtime, path, getImageNameFromURL(url));
    }

    public String getImageNameFromURL(String url) {
        int index = url.lastIndexOf('/');
        if (index > 0) {
            return url.substring(index + 1);
        }
        return url;
    }

}
