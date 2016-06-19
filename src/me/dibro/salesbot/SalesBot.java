package me.dibro.salesbot;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.TelegramApiException;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.CallbackQuery;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class SalesBot extends TelegramLongPollingBot {
    public static final Logger LOGGER = LoggerFactory.getLogger("SalesBot");
    public static final File PROPERTIES_FILE = new File("config.properties");

    private String botUsername;
    private String botToken;
    private Properties properties;
    private Database database;
    private Cache<String, Result> cache;

    public SalesBot() {
        if (!loadProperties()) System.exit(0);

        this.botUsername = properties.getProperty("botUsername");
        this.botToken = properties.getProperty("botToken");
        this.database = new Database(this);
        this.cache = CacheBuilder.newBuilder()
                .expireAfterAccess(10, TimeUnit.MINUTES)
                .build();
    }

    private boolean loadProperties() {
        properties = new Properties();

        properties.setProperty("dataBaseHost", "");
        properties.setProperty("dataBasePort", "");
        properties.setProperty("dataBaseName", "");
        properties.setProperty("dataBaseUser", "");
        properties.setProperty("dataBasePassword", "");

        properties.setProperty("botUsername", "");
        properties.setProperty("botToken", "");

        try {
            properties.load(new FileReader(PROPERTIES_FILE));
            return true;
        } catch (FileNotFoundException e) {
            info(PROPERTIES_FILE.getName() + " does not exist. Creating a new one.");
            info("Please configure it and try again.");

            try {
                properties.store(new FileWriter(PROPERTIES_FILE), "SalesBot config");
            } catch (IOException ex) {
                info("Exception caught while creating " + PROPERTIES_FILE.getName());
                ex.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText())
            handleTextMessage(update.getMessage());
        if (update.hasCallbackQuery())
            handleCallbackQuery(update.getCallbackQuery());
    }

    private void handleTextMessage(Message message) {
        String chatId = message.getChatId().toString();
        String text = message.getText();

        Result result = text.equalsIgnoreCase("/list")
                ? database.getAllProducts() : database.getProducts(text);

        if (result != null) {
            SendMessage send = new SendMessage();
            send.enableMarkdown(true);

            send.setText(prettyPrint(result.next()));

            InlineKeyboardMarkup markup = setupMarkup(result);
            if (markup != null) send.setReplayMarkup(markup);

            send.setChatId(chatId);
            try {
                Message sent = sendMessage(send);
                if (markup != null) {
                    String format = String.format("%d:%d",
                            sent.getChatId(), sent.getMessageId());
                    cache.put(format, result);
                }
            } catch (TelegramApiException e) {
                info("Exception caught while sending message");
                e.printStackTrace();
            }
        } else {
            SendMessage send = new SendMessage();
            send.setText("No products found!");

            send.setChatId(chatId);
            try {
                sendMessage(send);
            } catch (TelegramApiException e) {
                info("Exception caught while sending message");
                e.printStackTrace();
            }
        }
    }

    private void handleCallbackQuery(CallbackQuery callback) {
        boolean b;
        switch (callback.getData()) {
            case "back":
                b = true;
                break;
            case "next":
                b = false;
                break;
            default:
                info("Received invalid callback data");
                return;
        }

        Message message = callback.getMessage();
        Result result = cache.getIfPresent(String.format("%d:%d",
                message.getChatId(), message.getMessageId()));

        EditMessageText edit = new EditMessageText();
        edit.enableMarkdown(true);

        if (result != null) {
            Product product = b ? result.back() : result.next();
            edit.setText(prettyPrint(product));

            InlineKeyboardMarkup markup = setupMarkup(result);
            if (markup != null) edit.setReplyMarkup(markup);
        } else edit.setText("Session expired.");

        edit.setChatId(message.getChatId().toString());
        edit.setMessageId(message.getMessageId());
        try {
            editMessageText(edit);
        } catch (TelegramApiException e) {
            info("Exception caught while editing message text");
            e.printStackTrace();
        }

        AnswerCallbackQuery answer = new AnswerCallbackQuery();
        answer.setCallbackQueryId(callback.getId());
        try {
            answerCallbackQuery(answer);
        } catch (TelegramApiException e) {
            info("Exception caught while answering callback query");
            e.printStackTrace();
        }
    }

    private InlineKeyboardMarkup setupMarkup(Result result) {
        boolean back = result.hasBack(), next = result.hasNext();

        if (!back && !next) return null;

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();

        if (back) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText("Back");
            button.setCallbackData("back");
            row.add(button);
        }

        if (next) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText("Next");
            button.setCallbackData("next");
            row.add(button);
        }

        rows.add(row);
        markup.setKeyboard(rows);

        return markup;
    }

    private String prettyPrint(Product product) {
        return "*Name:*\n" +
                product.getName() +
                "\n*Short description:*\n" +
                product.getDescShort() +
                "\n*Source:*\n" +
                product.getSourceName() +
                '\n' +
                product.getSourceUrl() +
                "\n*In stock:*\n" +
                (product.isInStock() ? "Yes" : "No") +
                "\n*Discount:*\n" +
                product.getDiscount() +
                "\n*Price:*\n" +
                product.getPrice() +
                "\n*Description:*\n" +
                product.getDescFull() +
                "\n*Started:*\n" +
                product.getStarted() +
                "\n*Duration:*\n" +
                product.getDuration() +
                " days";
    }

    public Properties getProperties() {
        return properties;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    public static void info(Object object) {
        LOGGER.info(object.toString());
    }

    public static void main(String[] args) {
        TelegramBotsApi api = new TelegramBotsApi();

        try {
            api.registerBot(new SalesBot());
        } catch (TelegramApiException e) {
            info("Exception caught while registering Telegram bot");
            e.printStackTrace();
        }
    }
}