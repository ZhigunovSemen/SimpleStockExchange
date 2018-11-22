package ru.zhigunov.study.SimpleStockExchange.entity;

public enum OPERATION {

    BUY("b"), SELL("s");

    String shortName;

    OPERATION(String shortName) {
        this.shortName = shortName;
    }

    public static OPERATION valueOfShortName(String shortName) {
        for (OPERATION operation : OPERATION.values()) {
            if (operation.shortName.equals(shortName)) {
                return operation;
            }
        }
        return null;
    }


}
