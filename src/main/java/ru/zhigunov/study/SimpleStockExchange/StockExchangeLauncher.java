package ru.zhigunov.study.SimpleStockExchange;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.zhigunov.study.SimpleStockExchange.entity.Client;
import ru.zhigunov.study.SimpleStockExchange.entity.OPERATION;
import ru.zhigunov.study.SimpleStockExchange.entity.Order;
import ru.zhigunov.study.SimpleStockExchange.entity.STOCK;

import java.io.*;
import java.util.*;

public class StockExchangeLauncher {

    private static Logger LOGGER = LogManager.getLogger(StockExchangeLauncher.class);

    private InputStream inputStreamClientData;

    private InputStream inputStreamOrderData;

    private FileOutputStream saveResultOutputStream;

    /**
     * @param inputStreamClientData входной паток данныъ о клиентах
     * @param inputStreamOrderData входной поток данных о заявках на покупку/продажу
     * @throws IOException
     */
    public StockExchangeLauncher(InputStream inputStreamClientData,
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

    private Map<String, Client> clients = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    private List<Order> orders = new LinkedList<>();

    /** формируем очередь заявок по каждому виду ценных бумаг, чтобы не сравнивать заявки на разные бумеги */
    private Map<STOCK, List<Order>> orderQueue = new HashMap<>();


    private void loadClients() throws IOException {
        Scanner scanner = new Scanner(inputStreamClientData);
        while(scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] data = line.split("\t");
            int index = 0;
            String clientName = data[index++];
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
            int index = 0;
            String clientName = data[index++];
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
                    process(newOrder);
                    process(previousOrder);
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

    /**
     * Выполнение заявки и пометка как выполненная
     * @param order
     */
    private void process(Order order) {
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
        }
        order.setExecuted(true);
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
    }

    public void run() throws IOException {
        loadClients();
        loadOrders();
        processOrders();
        saveClients();
    }

}
