package com.tisqra.organization.domain.model;

/**
 * Subscription status enumeration
 */
public enum SubscriptionStatus {
    TRIAL,          // In trial period
    ACTIVE,         // Active subscription
    SUSPENDED,      // Temporarily suspended (e.g., payment issue)
    CANCELLED,      // Cancelled by user
    EXPIRED         // Subscription expired
}
