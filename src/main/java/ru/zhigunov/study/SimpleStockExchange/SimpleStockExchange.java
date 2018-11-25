package ru.zhigunov.study.SimpleStockExchange;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.zhigunov.study.SimpleStockExchange.entity.Client;
import ru.zhigunov.study.SimpleStockExchange.entity.OPERATION;
import ru.zhigunov.study.SimpleStockExchange.entity.Order;
import ru.zhigunov.study.SimpleStockExchange.entity.STOCK;
import ru.zhigunov.study.SimpleStockExchange.util.OrderProcessor;

import java.io.*;
import java.lang.reflect.Field;
import java.util.*;

public class SimpleStockExchange implements StockExchange {

    private static Logger LOGGER = LogManager.getLogger(SimpleStockExchange.class);

    private InputStream inputStreamClientData;

    private InputStream inputStreamOrderData;

    private FileOutputStream saveResultOutputStream;

    private OrderProcessor orderProcessor = new OrderProcessor();

    /**
     * @param inputStreamClientData входной паток данныъ о клиентах
     * @param inputStreamOrderData входной поток данных о заявках на покупку/продажу
     * @throws IOException
     */
    public SimpleStockExchange(InputStream inputStreamClientData,
                               InputStream inputStreamOrderData,
                               FileOutputStream saveResultOutputStream) throws IOException {
        if (null == inputStreamClientData || inputStreamClientData.available() == 0) {
            throw new IllegalArgumentException("Cannot read clients: clients data is null or not available");
        }
        if (null == inputStreamOrderData || inputStreamOrderData.available() == 0) {
            throw new IllegalArgumentException("Cannot read orders: orders data is null or not available");
        }
        this.inputStreamClientData = inputStreamClientData;
        this.inputStreamOrderData = inputStreamOrderData;
        this.saveResultOutputStream = saveResultOutputStream;

        for (STOCK stock : STOCK.values()) {
            orderQueue.put(stock, new LinkedList<>());
        }
    }

    /** Карта клиентов биржи, проиндексированная по имени, для быстрого доступа, и отсортированная, для порядка */
    private Map<String, Client> clients = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    private List<Order> orders = new LinkedList<>();

    /** формируем очередь заявок по каждому виду ценных бумаг, чтобы не сравнивать заявки на разные бумаги */
    private Map<STOCK, List<Order>> orderQueue = new HashMap<>();


    private void loadClients() throws IOException {
        Scanner scanner = new Scanner(inputStreamClientData);
        while(scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            String[] data = line.split("\t");
            int index = 0;
            if (line.length()< 5) continue;
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

    private void loadOrders() {
        Scanner scanner = new Scanner(inputStreamOrderData);
        while(scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] data = line.split("\t");
            if (line.length()< 5) continue;
            int index = 0;
            String clientName = data[index++];
            if (clientName.startsWith("//")) continue;
            Client client = clients.get(clientName);
            OPERATION operation = OPERATION.valueOfShortName(data[index++]);
            STOCK stock = STOCK.valueOf(data[index++]);
            Integer price = Integer.valueOf(data[index++]);
            Integer stockCount = Integer.valueOf(data[index]);

            Order order = new Order();
            order.setClient(client);
            order.setStock(stock);
            order.setOperation(operation);
            order.setPrice(price);
            order.setStockCount(stockCount);
            orders.add(order);
            LOGGER.debug(order.toString());
        }
        LOGGER.info("Load " + orders.size() + " orders.");
    }

    private void processOrders() {
        for (Order order : orders) {
            addOrderToListAndTryToExcahange(order);
        }
        long executedCount = orders.stream().filter(Order::isExecuted).count();
        LOGGER.debug("Executed: " + executedCount + " orders");
        long notExecuted = orders.size() - executedCount;
        LOGGER.debug("Not executed: " + notExecuted + " orders");
    }

    /** Метод проходит по всем заявкам и ищет подходящую заявку для выполнены, и если не найдено, добавляет её в соответствующую очередь
     *
     * Полагаю, что быстрее было бы не ходить по списку заявок, а складывать и брать подходящую заявку исходя из хеша от (цена, кол-во)
     * но что делать, если существуют несколько одинаковых заявок на одну ценную бумагу по цене и кол-ву?
     * по крайней мере, указанный ниже подход гарантирует выполнение одинаковых заявок про хронометражу
     *      - будет выполнена та завка, которая была размещена самой первой */
    private void addOrderToListAndTryToExcahange(Order newOrder) {
        STOCK stock = newOrder.getStock();
        String clientName = newOrder.getClient().getName();
        LOGGER.debug("   add " + newOrder + " to queue");

        for (Order previousOrder : orderQueue.get(stock)) {
            String prevClientName = previousOrder.getClient().getName();
            if (!prevClientName.equals(clientName)) {   // Проверка на заявки самому себе
                Integer newOrderPrice = newOrder.getPrice();
                Integer previousOrderPrice = previousOrder.getPrice();
                Integer newOrderStockCount = newOrder.getStockCount();
                Integer previousOrderStockCount = previousOrder.getStockCount();
                if (newOrderPrice.equals(previousOrderPrice)
                        && newOrderStockCount.equals(previousOrderStockCount)
                        && newOrder.getOperation() != previousOrder.getOperation()) {
                    orderProcessor.processOrder(newOrder);
                    orderProcessor.processOrder(previousOrder);
                    orderQueue.get(stock).remove(newOrder);
                    orderQueue.get(stock).remove(previousOrder);
                    LOGGER.debug("successful exchange between " + previousOrder.getClient().getName()
                        + " and " + newOrder.getClient().getName()
                        + " with " + newOrderStockCount +  " " + stock
                        + " by " + newOrderPrice + "$");
                    break;
                }
            }
        }
        if (!newOrder.isExecuted()) {
            orderQueue.get(stock).add(newOrder);
        }
    }



    private void saveClients() throws IOException {
        StringBuilder clientsInfo = new StringBuilder();
        for (Client client : clients.values()) {
            clientsInfo.append(client.getName()).append("\t");
            clientsInfo.append(client.getMoneyBalance()).append("\t");
            for (STOCK stock : STOCK.values()) {
                clientsInfo.append(client.getStockBalance().get(stock)).append("\t");
            }
            clientsInfo.append("\n");
        }
        saveResultOutputStream.write(clientsInfo.toString().getBytes());

        try {
            Field pathField = FileOutputStream.class.getDeclaredField("path");
            pathField.setAccessible(true);
            String path = (String) pathField.get(saveResultOutputStream);
            LOGGER.info("All clients saved to " + path);
        } catch (IllegalAccessException | NoSuchFieldException ex) {
            LOGGER.info("All clients saved.");
        }
    }

    @Override
    public void launch() throws IOException {
        loadClients();
        loadOrders();
        processOrders();
        saveClients();
    }

}
