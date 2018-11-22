package ru.zhigunov.study.SimpleStockExchange.entity;

public class Order {

    private Client client;

    private OPERATION operation;

    private STOCK stock;

    private Integer price = 0;

    private Integer stockCount = 0;

    private boolean executed = false;

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public OPERATION getOperation() {
        return operation;
    }

    public void setOperation(OPERATION operation) {
        this.operation = operation;
    }

    public STOCK getStock() {
        return stock;
    }

    public void setStock(STOCK stock) {
        this.stock = stock;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public Integer getStockCount() {
        return stockCount;
    }

    public void setStockCount(Integer stockCount) {
        this.stockCount = stockCount;
    }

    public boolean isExecuted() {
        return executed;
    }

    public void setExecuted(boolean executed) {
        this.executed = executed;
    }

    @Override
    public String toString() {
        return "Order{" +
                "client=" + client +
                ", operation=" + operation +
                ", stock=" + stock +
                ", price=" + price +
                ", stockCount=" + stockCount +
                ", executed=" + executed +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Order order = (Order) o;

        if (executed != order.executed) return false;
        if (client != null ? !client.equals(order.client) : order.client != null) return false;
        if (operation != order.operation) return false;
        if (stock != order.stock) return false;
        if (price != null ? !price.equals(order.price) : order.price != null) return false;
        return stockCount != null ? stockCount.equals(order.stockCount) : order.stockCount == null;
    }

    @Override
    public int hashCode() {
        int result = client != null ? client.hashCode() : 0;
        result = 31 * result + (operation != null ? operation.hashCode() : 0);
        result = 31 * result + (stock != null ? stock.hashCode() : 0);
        result = 31 * result + (price != null ? price.hashCode() : 0);
        result = 31 * result + (stockCount != null ? stockCount.hashCode() : 0);
        result = 31 * result + (executed ? 1 : 0);
        return result;
    }
}
