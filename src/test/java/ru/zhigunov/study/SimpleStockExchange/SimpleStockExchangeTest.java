package ru.zhigunov.study.SimpleStockExchange;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import ru.zhigunov.study.SimpleStockExchange.entity.Client;
import ru.zhigunov.study.SimpleStockExchange.entity.Order;
import ru.zhigunov.study.SimpleStockExchange.entity.STOCK;

import java.io.*;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

import static org.testng.Assert.*;

public class SimpleStockExchangeTest {

    private static Logger LOGGER = LogManager.getLogger(SimpleStockExchangeTest.class);

    private SimpleStockExchange simpleStockExchange;

    private File inputFileClients = new File("test/clients.txt");
    private File inputFileOrders = new File("test/orders.txt");
    private File outputFileClients = new File("test/result.txt");
    private File expectedFileClients = new File("test/result_expected.txt");

    @BeforeMethod
    public void setUp() throws Exception {
        LOGGER.info("------- START -------");
        if (outputFileClients.exists()) outputFileClients.delete();

        try (FileInputStream fisClients = new FileInputStream(inputFileClients);
             FileInputStream fisOrders = new FileInputStream(inputFileOrders);
             FileOutputStream fosResult = new FileOutputStream(outputFileClients)) {

            simpleStockExchange = new SimpleStockExchange(fisClients, fisOrders, fosResult);
            simpleStockExchange.launch();
        }
    }

    @AfterTest
    public void tearDown() throws Exception {
        LOGGER.info("------- END -------");
    }

    private void loadClients(Map<String, Client> clients, FileInputStream inputStreamClientData) throws IOException {
        Scanner scanner = new Scanner(inputStreamClientData);
        while(scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            String[] data = line.split("\t");
            if (line.length()< 5) continue;
            int index = 0;
            String clientName = data[index++];
            if (clientName.startsWith("//")) continue;
            Long moneyBalance = Long.parseLong(data[index++]);
            Map<STOCK, Integer> stocks = new HashMap<>();
            for (STOCK stock : STOCK.values()) {
                stocks.put(stock, Integer.valueOf(data[index++]));
            }

            Client client = new Client();
            client.setName(clientName);
            client.setMoneyBalance(moneyBalance);
            client.setStockBalance(stocks);
            clients.put(client.getName(), client);
            LOGGER.info("Load client " + client.getName());
        }
    }

    @Test
    public void test() throws Exception {

        Map<String, Client> clientsResult  = new HashMap<>(), clientsExpected = new HashMap<>();
        try (FileInputStream inputStreamClientData = new FileInputStream(outputFileClients);
                FileInputStream inputStreamClientDataExpected = new FileInputStream(expectedFileClients)) {
            loadClients(clientsResult, inputStreamClientData);
            loadClients(clientsExpected, inputStreamClientDataExpected);

            LOGGER.info("Analysing " + outputFileClients.toString());
            for (Map.Entry<String, Client> clientEntry : clientsResult.entrySet()) {
                Client client = clientEntry.getValue();
                Client clientExpected = clientsExpected.get(clientEntry.getKey());
                Assert.assertEquals(client, clientExpected);
                LOGGER.info("client " + client.getName() + " assertion true");
            }
        }
    }

}