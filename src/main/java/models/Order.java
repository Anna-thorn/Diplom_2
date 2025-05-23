package models;

import lombok.*;

/**
 * Модель создания заказа для API тестов.
 */
@Data
@AllArgsConstructor
public class Order {
    private String name;
    private OrderDetails order;
    private boolean success;

    @Data
    @AllArgsConstructor
    public static class OrderDetails {
        private int number;
    }
}