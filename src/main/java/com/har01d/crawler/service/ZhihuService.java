package com.har01d.crawler.service;

import com.har01d.crawler.bean.ImageInfo;
import com.har01d.crawler.bean.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class ZhihuService {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void init(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public PageInfo getPageInfo(String url) {
        List<PageInfo> results =
            this.jdbcTemplate.query("SELECT url,ctime,atime FROM zhihu_pages WHERE url=?", new RowMapper<PageInfo>() {
                @Override
                public PageInfo mapRow(ResultSet resultSet, int i) throws SQLException {
                    PageInfo pageInfo = new PageInfo();
                    pageInfo.setAtime(resultSet.getLong("atime"));
                    pageInfo.setCtime(resultSet.getLong("ctime"));
                    pageInfo.setUrl(resultSet.getString("url"));
                    return pageInfo;
                }
            }, url);

        if (results.isEmpty()) {
            return null;
        } else {
            return results.get(0);
        }
    }

    public long getPageAccessTime(String url) {
        List<Long> results =
            this.jdbcTemplate.query("SELECT atime FROM zhihu_pages WHERE url=?", new RowMapper<Long>() {
                @Override
                public Long mapRow(ResultSet resultSet, int i) throws SQLException {
                    return resultSet.getLong(1);
                }
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
            .query("SELECT url,size,path,ctime,mtime FROM zhihu_images WHERE url=?", new RowMapper<ImageInfo>() {
                @Override
                public ImageInfo mapRow(ResultSet resultSet, int i) throws SQLException {
                    ImageInfo imageInfo = new ImageInfo();
                    imageInfo.setMtime(resultSet.getLong("mtime"));
                    imageInfo.setCtime(resultSet.getLong("ctime"));
                    imageInfo.setUrl(resultSet.getString("url"));
                    imageInfo.setPath(resultSet.getString("path"));
                    imageInfo.setSize(resultSet.getLong("size"));
                    return imageInfo;
                }
            }, url);

        if (results.isEmpty()) {
            return null;
        } else {
            return results.get(0);
        }
    }

    public long getImageModifyTime(String url) {
        List<Long> results =
            this.jdbcTemplate.query("SELECT atime FROM zhihu_images WHERE url=?", new RowMapper<Long>() {
                @Override
                public Long mapRow(ResultSet resultSet, int i) throws SQLException {
                    return resultSet.getLong(1);
                }
            }, url);

        if (results.isEmpty()) {
            return 0;
        } else {
            return results.get(0);
        }
    }

    public int insertImage(String url, long size) {
        long ctime = System.currentTimeMillis();
        return this.jdbcTemplate.update("INSERT INTO zhihu_images (url, size, ctime) VALUES (?, ?)", url, size, ctime);
    }

    public int insertImage(String url, String path, long size) {
        long ctime = System.currentTimeMillis();
        return this.jdbcTemplate
            .update("INSERT INTO zhihu_images (url, path, size, ctime) VALUES (?, ?, ?, ?)", url, path, size, ctime);
    }

    public int updateImage(String url, String path) {
        long mtime = System.currentTimeMillis();
        return this.jdbcTemplate.update("UPDATE zhihu_images SET mtime=?,path=? WHERE url=?", mtime, path, url);
    }

}
