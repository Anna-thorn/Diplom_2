package models;

/**
 * Модель создания заказа для API тестов.
 */
public class Order {
    private String name;
    private OrderDetails order;
    private boolean success;

    public String getName() {return name;}
    public OrderDetails getOrder() {return order;}
    public boolean isSuccess() {return success;}

    public void setName(String name) {this.name = name;}
    public void setOrder(OrderDetails order) {this.order = order;}
    public void setSuccess(boolean success) {this.success = success;}

    public static class OrderDetails {
        private int number;
        public int getNumber() {return number;}
        public void setNumber(int number) {this.number = number;}
    }
}