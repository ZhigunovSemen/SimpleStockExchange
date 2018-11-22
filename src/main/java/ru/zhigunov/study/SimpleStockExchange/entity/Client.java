package ru.zhigunov.study.SimpleStockExchange.entity;

import java.util.Map;

public class Client {

    private String name;

    private Long moneyBalance = 0L;

    private Map<STOCK, Integer> stockBalance;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getMoneyBalance() {
        return moneyBalance;
    }

    public void setMoneyBalance(Long moneyBalance) {
        this.moneyBalance = moneyBalance;
    }

    public Map<STOCK, Integer> getStockBalance() {
        return stockBalance;
    }

    public void setStockBalance(Map<STOCK, Integer> stockBalance) {
        this.stockBalance = stockBalance;
    }

    @Override
    public String toString() {
        return "Client{" +
                "name='" + name + '\'' +
                '}';
    }
}
