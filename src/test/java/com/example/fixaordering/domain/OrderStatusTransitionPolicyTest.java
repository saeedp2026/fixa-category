package com.example.fixaordering.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrderStatusTransitionPolicyTest {

    @Test
    @DisplayName("FINAL_ORDER may go to technician accepted or be cancelled by anyone")
    void finalOrderAllowedTransitions() {
        assertThat(OrderStatus.FINAL_ORDER.canTransitionTo(OrderStatus.TECHNICIAN_ACCEPTED)).isTrue();
        assertThat(OrderStatus.FINAL_ORDER.canTransitionTo(OrderStatus.CLIENT_CANCELLED)).isTrue();
        assertThat(OrderStatus.FINAL_ORDER.canTransitionTo(OrderStatus.ADMIN_CANCELLED)).isTrue();
    }

    @Test
    @DisplayName("FINAL_ORDER cannot skip to in-progress, completed or itself")
    void finalOrderRejectedTransitions() {
        assertThat(OrderStatus.FINAL_ORDER.canTransitionTo(OrderStatus.FINAL_ORDER)).isFalse();
        assertThat(OrderStatus.FINAL_ORDER.canTransitionTo(OrderStatus.ORDER_IN_PROGRESS)).isFalse();
        assertThat(OrderStatus.FINAL_ORDER.canTransitionTo(OrderStatus.ORDER_COMPLETED)).isFalse();
    }

    @Test
    @DisplayName("Happy progression is allowed step by step")
    void happyProgression() {
        assertThat(OrderStatus.FINAL_ORDER.canTransitionTo(OrderStatus.TECHNICIAN_ACCEPTED)).isTrue();
        assertThat(OrderStatus.TECHNICIAN_ACCEPTED.canTransitionTo(OrderStatus.ORDER_IN_PROGRESS)).isTrue();
        assertThat(OrderStatus.ORDER_IN_PROGRESS.canTransitionTo(OrderStatus.ORDER_COMPLETED)).isTrue();
    }

    @Test
    @DisplayName("Technical progression cannot be skipped through")
    void skippedProgressionRejected() {
        assertThat(OrderStatus.TECHNICIAN_ACCEPTED.canTransitionTo(OrderStatus.ORDER_COMPLETED)).isFalse();
        assertThat(OrderStatus.TECHNICIAN_ACCEPTED.canTransitionTo(OrderStatus.FINAL_ORDER)).isFalse();
        assertThat(OrderStatus.ORDER_IN_PROGRESS.canTransitionTo(OrderStatus.FINAL_ORDER)).isFalse();
    }

    @Test
    @DisplayName("Terminal states accept no further transitions")
    void terminalStates() {
        assertThat(OrderStatus.ORDER_COMPLETED.canTransitionTo(OrderStatus.FINAL_ORDER)).isFalse();
        assertThat(OrderStatus.CLIENT_CANCELLED.canTransitionTo(OrderStatus.FINAL_ORDER)).isFalse();
        assertThat(OrderStatus.ADMIN_CANCELLED.canTransitionTo(OrderStatus.FINAL_ORDER)).isFalse();
        assertThat(OrderStatus.ORDER_COMPLETED.canTransitionTo(OrderStatus.ORDER_COMPLETED)).isFalse();
        assertThat(OrderStatus.CLIENT_CANCELLED.canTransitionTo(OrderStatus.TECHNICIAN_ACCEPTED)).isFalse();
        assertThat(OrderStatus.ADMIN_CANCELLED.canTransitionTo(OrderStatus.ORDER_IN_PROGRESS)).isFalse();
    }
}