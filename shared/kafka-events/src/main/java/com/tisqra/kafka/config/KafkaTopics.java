package com.tisqra.kafka.config;

/**
 * Centralized Kafka topic names used across services.
 */
public final class KafkaTopics {

    private KafkaTopics() {}

    public static final String ORGANIZATION_CREATED = "organization.created";
    public static final String SUBSCRIPTION_UPGRADED = "subscription.upgraded";

    public static final String EVENT_PUBLISHED = "event.published";

    public static final String TICKET_CATEGORY_CREATED = "ticket.category.created";

    public static final String ORDER_CREATED = "order.created";
    public static final String PAYMENT_COMPLETED = "payment.completed";
    public static final String PAYMENT_FAILED = "payment.failed";

    public static final String TICKET_GENERATED = "ticket.generated";
    public static final String TICKET_VALIDATED = "ticket.validated";
    public static final String TICKET_TRANSFERRED = "ticket.transferred";

    public static final String NOTIFICATION_EMAIL_SEND = "notification.email.send";
    public static final String NOTIFICATION_PUSH_SEND = "notification.push.send";

    public static final String ANALYTICS_SALES_RECORDED = "analytics.sales.recorded";

    /**
     * Used for dead-letter topics (DLQs).
     */
    public static final String DLQ_SUFFIX = ".dlq";
}

