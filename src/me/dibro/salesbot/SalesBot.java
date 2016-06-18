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
    private static final File propertiesFile = new File("config.properties");

    public static final Logger logger = LoggerFactory.getLogger("SalesBot");

    private String botUsername;
    private String botToken;
    private Properties properties;
    private Database database;
    private Cache<String, Result> cache;

    public SalesBot() {
        if (!loadProperties()) return;

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
            properties.load(new FileReader(propertiesFile));
            return true;
        } catch (FileNotFoundException e) {
            info(propertiesFile.getName() + " does not exist. Creating a new one.");
            info("Please configure it and try again.");

            try {
                properties.store(new FileWriter(propertiesFile), "SalesBot config");
            } catch (IOException ex) {
                info("\nException caught while creating " + propertiesFile.getName());
                ex.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public void onUpdateReceived(Update update) {
        info(update);
        if (update.hasMessage() && update.getMessage().hasText())
            handleTextMessage(update.getMessage());
        if (update.hasCallbackQuery())
            handleCallbackQuery(update.getCallbackQuery());
    }

    private void handleTextMessage(Message message) {
        String chatId = message.getChatId().toString();
        String text = message.getText();

        Result result = database.getProducts(text);
        if (result.hasNext()) {
            SendMessage send = new SendMessage();

            send.setText(result.next().toString());

            InlineKeyboardMarkup markup = setupMarkup(result);
            if (markup != null) send.setReplayMarkup(markup);

            send.setChatId(chatId);
            try {
                Message sent = sendMessage(send);
                if (markup != null) cache.put(String.format("%d:%d",
                        sent.getChatId(), sent.getMessageId()), result);
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

        if (result != null) {
            Product product = b ? result.back() : result.next();
            edit.setText(product.toString());

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
        logger.info(object.toString());
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