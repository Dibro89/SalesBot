package me.dibro.salesbot;

import org.telegram.telegrambots.TelegramApiException;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

import java.io.*;
import java.util.Properties;

public class SalesBot extends TelegramLongPollingBot {
    private static final File propertiesFile = new File("config.properties");

    private Properties properties;
    private Database database;

    public SalesBot() {
        loadProperties();
        database = new Database(this);
    }

    private void loadProperties() {
        properties = new Properties();

        properties.setProperty("dataBaseHost", "");
        properties.setProperty("dataBasePort", "");
        properties.setProperty("dataBaseName", "");
        properties.setProperty("dataBaseUser", "");
        properties.setProperty("dataBasePassword", "");

        properties.setProperty("botUserName", "");
        properties.setProperty("botToken", "");

        try {
            properties.load(new FileReader(propertiesFile));
        } catch (FileNotFoundException e) {
            System.out.println("Error: " + propertiesFile.getName() + " not found! Creating a new one.");
            System.out.println("Please configure it and try again.");

            try {
                properties.store(new FileWriter(propertiesFile), "SalesBot config");
            } catch (IOException ex) {
                System.out.println();
                System.out.println("Exception caught while creating " + propertiesFile.getName());
                ex.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleTextMessage(Message message) {
        String chatId = message.getChatId().toString();
        String text = message.getText();

        Product[] products = database.getProducts(text);
        if (products.length > 0) {

        } else sendTextMessage(chatId, "");
    }

    private void sendTextMessage(String chatId, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(text);

        try {
            sendMessage(sendMessage);
        } catch (TelegramApiException e) {
            System.out.println("Exception caught while sending message");
            e.printStackTrace();
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText())
            handleTextMessage(update.getMessage());
    }

    @Override
    public String getBotUsername() {
        return properties.getProperty("botUserName");
    }

    @Override
    public String getBotToken() {
        return properties.getProperty("botToken");
    }

    public Properties getProperties() {
        return properties;
    }

    public static void main(String[] args) {
        TelegramBotsApi api = new TelegramBotsApi();

        try {
            api.registerBot(new SalesBot());
        } catch (TelegramApiException e) {
            System.out.println("Exception caught while registering Telegram bot");
            e.printStackTrace();
        }
    }
}