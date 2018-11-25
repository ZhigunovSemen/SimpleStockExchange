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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Client client = (Client) o;

        if (name != null ? !name.equals(client.name) : client.name != null) return false;
        if (moneyBalance != null ? !moneyBalance.equals(client.moneyBalance) : client.moneyBalance != null)
            return false;
        return stockBalance != null ? stockBalance.equals(client.stockBalance) : client.stockBalance == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (moneyBalance != null ? moneyBalance.hashCode() : 0);
        result = 31 * result + (stockBalance != null ? stockBalance.hashCode() : 0);
        return result;
    }
}
