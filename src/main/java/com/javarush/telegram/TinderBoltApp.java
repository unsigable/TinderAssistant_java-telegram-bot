package com.javarush.telegram;

import com.javarush.telegram.ChatGPTService;
import com.javarush.telegram.DialogMode;
import com.javarush.telegram.MultiSessionTelegramBot;
import com.javarush.telegram.UserInfo;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.ArrayList;

public class TinderBoltApp extends MultiSessionTelegramBot {
    public static final String TELEGRAM_BOT_NAME = "BOT_NAME"; //TODO: добавь имя бота в кавычках
    public static final String TELEGRAM_BOT_TOKEN = "BOT_TOKEN"; //TODO: добавь токен бота в кавычках
    public static final String OPEN_AI_TOKEN = "GPT_API_KEY"; //TODO: добавь токен ChatGPT в кавычках

    private ChatGPTService chatGPT = new ChatGPTService(OPEN_AI_TOKEN);
    private DialogMode currentMode = null;
    private ArrayList<String> list = new ArrayList<>();

    private UserInfo me;
    private  UserInfo she;
    private int questionCount;

    public TinderBoltApp() {
        super(TELEGRAM_BOT_NAME, TELEGRAM_BOT_TOKEN);
    }

    @Override
    public void onUpdateEventReceived(Update update) {
        //TODO: основной функционал бота будем писать здесь
        String message = getMessageText();

        if (message.equals("/start")) {
            currentMode = DialogMode.MAIN;
            sendPhotoMessage("main");
            String text = loadMessage("main");
            sendTextMessage(text);

            showMainMenu("главное меню бота", "/start",
                    "генерация Tinder-профля \uD83D\uDE0E", "/profile",
                    "сообщение для знакомства \uD83E\uDD70", "/opener",
                    "переписка от вашего имени \uD83D\uDE08", "/message",
                    "переписка со звездами \uD83D\uDD25", "/date",
                    "задать вопрос чату GPT \uD83E\uDDE0", "/gpt");
            return;
        }

        // Command GPT
        if (message.equals("/gpt")) {
            currentMode = DialogMode.GPT;
            sendPhotoMessage("gpt");
            String text = loadMessage("gpt");
            sendTextMessage(text);
            return;
        }

        if (currentMode == DialogMode.GPT && !isMessageCommand()) {
            String prompt = loadPrompt("gpt");

            Message msg = sendTextMessage("Подождите несколько секунд - ChatGPT думает...");
            String answer = chatGPT.sendMessage(prompt, message);
            updateTextMessage(msg, answer);
            return;
        }

        // Command DATE
        if (message.equals("/date")) {
            currentMode = DialogMode.DATE;
            sendPhotoMessage("date");
            String text = loadMessage("date");
            sendTextButtonsMessage(text,
                    "Ариана Гранде ❤\uFE0F", "date_grande",
                    "Марго Робби ❤\uFE0F❤\uFE0F", "date_robbie",
                    "Скарлетт Йоханссон ❤\uFE0F❤\uFE0F❤\uFE0F", "date_scarlett",
                    "Эмили Блант ❤\uFE0F❤\uFE0F❤\uFE0F❤\uFE0F", "date_emily",
                    "Блейк Лайвли ❤\uFE0F❤\uFE0F❤\uFE0F❤\uFE0F❤\uFE0F", "date_lively");
            return;
        }

        if (currentMode == DialogMode.DATE && !isMessageCommand()) {
            String query = getCallbackQueryButtonKey();
            // if (query.equals("date_margo")) {
            //    sendPhotoMessage("date_margo");
            //    return;
            //}
            if (query.startsWith("date_")) {
                sendPhotoMessage(query);
                sendTextMessage("Отличный выбор! \nТвоя задача пригласить девушку на свидание ❤\uFE0F");

                String prompt = loadPrompt(query);
                chatGPT.setPrompt(prompt);
                return;
            }

            Message msg = sendTextMessage("Девушка пишет сообщение...");
            String answer = chatGPT.addMessage(message);
            updateTextMessage(msg, answer);
            return;
        }

        // Command MESSAGE
        if (message.equals("/message")) {
            currentMode = DialogMode.MESSAGE;
            sendPhotoMessage("message");
            sendTextButtonsMessage("Пришлите в чат Вашу переписку",
                    "Следующее сообщение", "message_next",
                    "Пригласить на свидание", "message_date");
            return;
        }

        if (currentMode == DialogMode.MESSAGE && !isMessageCommand()) {
            String query = getCallbackQueryButtonKey();
            if (query.startsWith("message_")) {
                String prompt = loadPrompt(query);
                String userChatHistory = String.join("\n\n", list);

                Message msg = sendTextMessage("Подождите несколько секунд - ChatGPT думает...");
                String answer = chatGPT.sendMessage(prompt, userChatHistory); //10 sec
                updateTextMessage(msg, answer);
                return;
            }

            list.add(message);
            return;
        }

        // Command PROFILE
        if (message.equals("/profile")) {
            currentMode = DialogMode.PROFILE;
            sendPhotoMessage("profile");

            me = new UserInfo();
            questionCount = 1;
            sendTextMessage("Как Вас зовут?");
            return;
        }

        if (currentMode == DialogMode.PROFILE && !isMessageCommand()) {
            switch (questionCount) {
                case 1:
                    me.name = message;
                    questionCount = 2;
                    sendTextMessage("Укажите Ваш пол");
                    return;
                case 2:
                    me.sex = message;
                    questionCount = 3;
                    sendTextMessage("Сколько Вам лет?");
                    return;
                case 3:
                    me.age = message;
                    questionCount = 4;
                    sendTextMessage("Из какого Вы города?");
                    return;
                case 4:
                    me.city = message;
                    questionCount = 5;
                    sendTextMessage("кем Вы работаете?");
                    return;
                case 5:
                    me.occupation = message;
                    questionCount = 6;
                    sendTextMessage("Какое у Вас хобби?");
                    return;
                case 6:
                    me.hobby = message;
                    questionCount = 7;
                    sendTextMessage("Сколько Вы зарабатываете?");
                    return;
                case 7:
                    me.wealth = message;
                    questionCount = 8;
                    sendTextMessage("Что Вам НЕ нравится в людях?");
                    return;
                case 8:
                    me.annoys = message;
                    questionCount = 9;
                    sendTextMessage("Какова Ваша цель знакомства?");
                    return;
                case 9:
                    me.goals = message;

                    String aboutMyself = me.toString();
                    String prompt = loadPrompt("profile");

                    Message msg = sendTextMessage("Подождите несколько секунд - \uD83E\uDDE0 ChatGPT думает... ✍\uD83C\uDFFD");
                    String answer = chatGPT.sendMessage(prompt, aboutMyself);
                    updateTextMessage(msg, answer);
                    return;
            }
        }

        // Command OPENER
        if (message.equals("/opener")) {
            currentMode = DialogMode.OPENER;
            sendPhotoMessage("opener");

            she = new UserInfo();
            questionCount = 1;
            sendTextMessage("Как её зовут?");
            return;
        }

        if (currentMode == DialogMode.OPENER && !isMessageCommand()) {
            switch (questionCount) {
                case 1:
                    she.name = message;
                    questionCount = 2;
                    sendTextMessage("Сколько ей лет?");
                    return;
                case 2:
                    she.age = message;
                    questionCount = 3;
                    sendTextMessage("Из какого она города?");
                    return;
                case 3:
                    she.city = message;
                    questionCount = 4;
                    sendTextMessage("Чем она занимается?");
                    return;
                case 4:
                    she.occupation = message;
                    questionCount = 5;
                    sendTextMessage("Чем она интересуется?");
                    return;
                case 5:
                    she.hobby = message;
                    questionCount = 6;
                    sendTextMessage("Она очень красивая?");
                    return;
                case 6:
                    she.handsome = message;
                    questionCount = 7;
                    sendTextMessage("Цель знакомства?");
                    return;
                case 7:
                    she.goals = message;

                    String aboutGirl = she.toString();;
                    String prompt = loadPrompt("opener");

                    Message msg = sendTextMessage("Подождите несколько секунд - \uD83E\uDDE0 ChatGPT думает... ✍\uD83C\uDFFD");
                    String answer = chatGPT.sendMessage(prompt, aboutGirl);
                    updateTextMessage(msg, answer);
                    return;
            }

            return;
        }

    }

    public static void main(String[] args) throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(new TinderBoltApp());
    }
}























