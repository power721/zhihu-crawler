package com.har01d.crawler.util;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;

public final class ImageUtil {

    public static boolean isValidImage(File file) throws IOException {
        if (!file.exists()) {
            return false;
        }

        String type = Files.probeContentType(file.toPath());
        switch (type) {
            case "image/jpeg":
                return isValidJpeg(file);
            case "image/png":
                return isValidPng(file);
            default:
                return false;
        }
    }

    private static boolean isValidJpeg(File file) throws IOException {
        try (RandomAccessFile fh = new RandomAccessFile(file, "r")) {
            long length = fh.length();
            if (length < 1024L) { // Or whatever
                return false;
            }

            fh.seek(length - 2);
            byte[] eoi = new byte[2];
            fh.readFully(eoi);
            return eoi[0] == (byte) 0xFF && eoi[1] == (byte) 0xD9; // FF D9
        }
    }

    private static boolean isValidPng(File file) throws IOException {
        try (RandomAccessFile fh = new RandomAccessFile(file, "r")) {
            long length = fh.length();
            if (length < 1024L) { // Or whatever
                return false;
            }

            byte[] eoi = new byte[4];
            fh.readFully(eoi);
            // \x89PNG\x0d\x0a\x1a\x0a     PNG image data
            return eoi[0] == (byte) 0x89 && eoi[1] == (byte) 0x50 && eoi[2] == (byte) 0x4E && eoi[3] == (byte) 0x47;
        }
    }

}
