package ru.zhigunov.study.SimpleStockExchange;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class Main {

    private static Logger LOGGER = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        try (FileInputStream fisClients = new FileInputStream(new File("clients.txt"));
             FileInputStream fisOrders = new FileInputStream(new File("orders.txt"));
             FileOutputStream outputStream = new FileOutputStream(new File("result.txt"))) {

            LOGGER.info("-----------------------");
            LOGGER.info("Start");
            long ms = System.currentTimeMillis();
            StockExchange stockExchange = new SimpleStockExchange(fisClients, fisOrders, outputStream);
            stockExchange.launch();

            long ms2 = System.currentTimeMillis();
            LOGGER.info("End. Running time is " + (ms2 - ms) + " ms");
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }
}
