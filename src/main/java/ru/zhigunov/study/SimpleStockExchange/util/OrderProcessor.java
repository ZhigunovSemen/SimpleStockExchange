package ru.zhigunov.study.SimpleStockExchange.util;

import ru.zhigunov.study.SimpleStockExchange.entity.Client;
import ru.zhigunov.study.SimpleStockExchange.entity.OPERATION;
import ru.zhigunov.study.SimpleStockExchange.entity.Order;
import ru.zhigunov.study.SimpleStockExchange.entity.STOCK;

import java.util.Map;

public class OrderProcessor {

    /**
     * Выполнение заявки и пометка как выполненная
     * @param order
     */
    public void processOrder(Order order) {
        Client client = order.getClient();
        Integer price = order.getPrice();
        Integer stockCount = order.getStockCount();
        STOCK stock = order.getStock();
        OPERATION operation = order.getOperation();

        if (operation == OPERATION.BUY) {
            client.setMoneyBalance(client.getMoneyBalance() - (price * stockCount));
            Map<STOCK, Integer> balances = client.getStockBalance();
            balances.put(stock, balances.get(stock) + stockCount);
        } else if (operation == OPERATION.SELL) {
            client.setMoneyBalance(client.getMoneyBalance() + (price * stockCount));
            Map<STOCK, Integer> balances = client.getStockBalance();
            balances.put(stock, balances.get(stock) - stockCount);
        } else {
            throw new RuntimeException("Operation must be SELL or BUY.");
        }
        order.setExecuted(true);
    }
}
