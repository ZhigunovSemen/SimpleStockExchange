package ru.zhigunov.study.SimpleStockExchange.util;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import ru.zhigunov.study.SimpleStockExchange.entity.Client;
import ru.zhigunov.study.SimpleStockExchange.entity.OPERATION;
import ru.zhigunov.study.SimpleStockExchange.entity.Order;
import ru.zhigunov.study.SimpleStockExchange.entity.STOCK;

import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.*;

public class OrderProcessorTest {

    private OrderProcessor orderProcessor;

    @BeforeMethod
    public void setUp() throws Exception {
        orderProcessor = new OrderProcessor();
    }

    @Test
    public void testProcessOrder() {
        Client client = new Client();
        client.setName("C1");
        client.setMoneyBalance(1000L);
        Map<STOCK, Integer> balances = new HashMap<>();
        balances.put(STOCK.A, 1);
        balances.put(STOCK.B, 2);
        client.setStockBalance(balances);

        Order order1 = new Order();
        order1.setClient(client);
        order1.setOperation(OPERATION.SELL);
        order1.setStock(STOCK.A);
        order1.setStockCount(1);
        order1.setPrice(10);

        Order order2 = new Order();
        order2.setClient(client);
        order2.setOperation(OPERATION.BUY);
        order2.setStock(STOCK.B);
        order2.setStockCount(1);
        order2.setPrice(20);

        orderProcessor.processOrder(order1);
        orderProcessor.processOrder(order2);

        Assert.assertTrue(client.getMoneyBalance().equals(990L));
        Assert.assertTrue(client.getStockBalance().get(STOCK.A).equals(0));
        Assert.assertTrue(client.getStockBalance().get(STOCK.B).equals(3));

    }

}