package com.har01d.crawler.exception;

import java.io.IOException;

public class ServerSideException extends IOException {

    public ServerSideException() {
    }

    public ServerSideException(String message) {
        super(message);
    }

    public ServerSideException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServerSideException(Throwable cause) {
        super(cause);
    }

}
