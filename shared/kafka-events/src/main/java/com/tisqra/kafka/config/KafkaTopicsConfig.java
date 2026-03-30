package com.tisqra.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Ensures Kafka topics exist on startup (useful for local Docker runs).
 */
@Configuration
public class KafkaTopicsConfig {

    private static final int PARTITIONS = 3;
    private static final short REPLICATION_FACTOR = 1;

    @Bean
    public NewTopic organizationCreatedTopic() {
        return createTopic(KafkaTopics.ORGANIZATION_CREATED);
    }

    @Bean
    public NewTopic subscriptionUpgradedTopic() {
        return createTopic(KafkaTopics.SUBSCRIPTION_UPGRADED);
    }

    @Bean
    public NewTopic eventPublishedTopic() {
        return createTopic(KafkaTopics.EVENT_PUBLISHED);
    }

    @Bean
    public NewTopic ticketCategoryCreatedTopic() {
        return createTopic(KafkaTopics.TICKET_CATEGORY_CREATED);
    }

    @Bean
    public NewTopic orderCreatedTopic() {
        return createTopic(KafkaTopics.ORDER_CREATED);
    }

    @Bean
    public NewTopic paymentCompletedTopic() {
        return createTopic(KafkaTopics.PAYMENT_COMPLETED);
    }

    @Bean
    public NewTopic paymentFailedTopic() {
        return createTopic(KafkaTopics.PAYMENT_FAILED);
    }

    @Bean
    public NewTopic ticketGeneratedTopic() {
        return createTopic(KafkaTopics.TICKET_GENERATED);
    }

    @Bean
    public NewTopic ticketValidatedTopic() {
        return createTopic(KafkaTopics.TICKET_VALIDATED);
    }

    @Bean
    public NewTopic ticketTransferredTopic() {
        return createTopic(KafkaTopics.TICKET_TRANSFERRED);
    }

    @Bean
    public NewTopic notificationEmailSendTopic() {
        return createTopic(KafkaTopics.NOTIFICATION_EMAIL_SEND);
    }

    @Bean
    public NewTopic notificationPushSendTopic() {
        return createTopic(KafkaTopics.NOTIFICATION_PUSH_SEND);
    }

    @Bean
    public NewTopic analyticsSalesRecordedTopic() {
        return createTopic(KafkaTopics.ANALYTICS_SALES_RECORDED);
    }

    private NewTopic createTopic(String topic) {
        return new NewTopic(topic, PARTITIONS, REPLICATION_FACTOR);
    }

    @SuppressWarnings("unused")
    private NewTopic createDlqTopic(String topic) {
        return createTopic(topic + KafkaTopics.DLQ_SUFFIX);
    }
}

