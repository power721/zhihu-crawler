CREATE USER 'crawler'@'localhost' IDENTIFIED BY 'crawler-2015@zhihu.com';

GRANT SELECT,INSERT,UPDATE,DELETE,CREATE,DROP ON crawler.* TO 'crawler'@'localhost';

CREATE DATABASE crawler CHARACTER SET = UTF-8;

DROP TABLE IF EXISTS `zhihu_pages`;
CREATE TABLE zhihu_pages (
    id int(9) NOT NULL AUTO_INCREMENT,
    url VARCHAR(255) NOT NULL,
    atime bigint(11) DEFAULT NULL,
    ctime bigint(11) NOT NULL,
    PRIMARY KEY (`id`)
    );

DROP TABLE IF EXISTS `zhihu_images`;
CREATE TABLE zhihu_images (
    id int(9) NOT NULL AUTO_INCREMENT,
    url VARCHAR(255) NOT NULL,
    path VARCHAR(255) DEFAULT NULL,
    `size` bigint(11) DEFAULT NULL,
    ctime bigint(11) NOT NULL,
    mtime bigint(11) DEFAULT NULL,
    PRIMARY KEY (`id`)
    );
