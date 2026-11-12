package com.training.assetmanagement.notification.domain;

// Hai bên khai lệch một tham số là RabbitMQ từ chối (PRECONDITION_FAILED)
public final class EmailQueue {

    public static final String EXCHANGE = "email.exchange";
    public static final String QUEUE = "email.queue";
    public static final String RETRY_QUEUE = "email.retry.queue";
    public static final String ROUTING_KEY = "email.send";
    public static final String RETRY_ROUTING_KEY = "email.retry";

    private EmailQueue() {}
}
