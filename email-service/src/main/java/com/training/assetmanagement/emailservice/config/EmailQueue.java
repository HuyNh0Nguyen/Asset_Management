package com.training.assetmanagement.emailservice.config;

public final class EmailQueue {

    public static final String EXCHANGE = "email.exchange";
    public static final String QUEUE = "email.queue";
    public static final String RETRY_QUEUE = "email.retry.queue";
    public static final String ROUTING_KEY = "email.send";
    public static final String RETRY_ROUTING_KEY = "email.retry";

    // Số lần hoãn liên tiếp vì CSDL không tới được; số lần thử lại nằm ở email_logs.attempt_count
    public static final String DEFERAL_HEADER = "x-email-deferral";

    private EmailQueue() {}
}
