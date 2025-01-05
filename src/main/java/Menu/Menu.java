package Menu;

import UrlShortener.URLShortener;
import config.Config;
import url.Url;
import url.UrlStorage;
import user.User;
import user.UserStorage;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;

interface Option {
    void execute();
}

public class Menu {
    private final String dateFormat = "dd/MM/yyyy hh:mm:ss";
    private final Scanner scanner;
    private final Config cfg;
    private final UserStorage userStorage;
    private final UrlStorage urlStorage;
    private boolean running = true;
    private User currUser;

    private class MenuItem {
        private final String name;
        private final Option onSelected;

        public MenuItem(String name, Option onSelected) {
            this.name = name;
            this.onSelected = onSelected;
        }

        public String getName() {
            return name;
        }

    }

    List<MenuItem> options = new ArrayList<>();

    public Menu(UserStorage userStorage, UrlStorage urlStorage, InputStream input, Config cfg) {
        this.userStorage = userStorage;
        this.urlStorage = urlStorage;
        this.scanner = new Scanner(input);
        this.cfg = cfg;
        options.add(new MenuItem("1. Вход/Смена пользователя", this::auth));
        options.add(new MenuItem("2. Добавление ссылки", this::addUrl));
        options.add(new MenuItem("3. Удаление ссылки", this::deleteUrl));
        options.add(new MenuItem("4. Обновление ссылки", this::updateUrl));
        options.add(new MenuItem("5. Переход по ссылке", this::openUrl));
        options.add(new MenuItem("6. Выход", this::stop));
    }

    public void run() {
        while (running) {
            show();
            Integer option = getOption();
            options.get(option - 1).onSelected.execute();
        }
    }

    private Integer getOption() {
        Integer option = null;
        while (option == null || option < 1 || option > this.options.size() + 1) {
            try {
                option = scanner.nextInt();
            } catch (InputMismatchException e) {
                System.out.println("Invalid option");
            }
            scanner.nextLine();
        }
        return option;
    }

    private void show() {
        for (MenuItem option : options) {
            System.out.println(option.getName());
        }
    }

    private void auth() {
        do {
            System.out.println("Введите свой идентификатор (пустое поле если не зарегистрированы):");
            String userId = scanner.nextLine();
            if (userId.isEmpty()) {
                currUser = userStorage.Create();
            } else {
                currUser = userStorage.Get(userId);
            }
            if (currUser == null) {
                System.out.println("Authentication failed");
            } else {
                System.out.println("Вы авторизованы как " + currUser.getID());
            }
        } while (currUser == null);
    }

    private void addUrl() {
        if (unauthorized()) {
            System.out.println("Необходима авторизация!");
            return;
        }
        System.out.println("Введите ссылку:");
        String urlString = scanner.nextLine();

        System.out.printf("Введите срок действия ссылки формате %s (пустое поле для значения по умолчанию):%n", dateFormat);
        String dateString = scanner.nextLine();
        Date expirationDate = null;
        try {
            expirationDate = new SimpleDateFormat(dateFormat).parse(dateString);
        } catch (ParseException e) {
            System.out.println("Неверный формат даты, установлено значение по умолчанию");
            expirationDate = new Date(new Date().getTime() + cfg.URLExpirationTimeMillis);
        }

        System.out.println("Введите число переходов (пустое поле для значения по умолчанию):");
        int accessLimit;
        try {
            accessLimit = scanner.nextInt();
        } catch (InputMismatchException e) {
            System.out.println("Установлено значение по умолчанию");
            accessLimit = cfg.URLAccessLimit;
        }
        scanner.nextLine();


        String shortUrl = URLShortener.GenerateShortURL(urlString, this.currUser.getID());
        Url url = new Url(urlString, shortUrl, this.currUser.getID(), expirationDate, accessLimit);
        if (this.urlStorage.RemoveUrl(urlString, this.currUser.getID())) {
            System.out.println("Ссылка уже существует, значение будет обновлено");
        }
        this.urlStorage.AddUrl(url);
        System.out.println("Ссылка добавлена:\n" + shortUrl);
    }

    private void deleteUrl() {
        if (unauthorized()) {
            System.out.println("Необходима авторизация!");
            return;
        }
        System.out.println("Введите ссылку:");
        String urlString = scanner.nextLine();
        boolean ok = this.urlStorage.RemoveUrl(urlString, this.currUser.getID());
        if (ok) {
            System.out.println("Ссылка удалена");
        } else {
            System.out.println("Ссылка не найдена");
        }
    }

    private void updateUrl() {
        this.addUrl();
    }

    private void openUrl() {
        if (unauthorized()) {
            System.out.println("Необходима авторизация!");
            return;
        }
        System.out.println("Введите короткую ссылку:");
        String urlString = scanner.nextLine();
        Url url = this.urlStorage.GetUrlByShortUrl(urlString, this.currUser.getID());
        if (url == null) {
            System.out.println("Ссылка не найдена");
            return;
        }
        if (url.accessLimitReached()) {
            System.out.println("Лимит переходов исчерпан");
            this.urlStorage.RemoveUrl(urlString, this.currUser.getID());
            return;
        }
        if (url.isExpired()) {
            System.out.println("Ссылка просрочена");
            this.urlStorage.RemoveUrl(urlString, this.currUser.getID());
            return;
        }
        try {
            Desktop.getDesktop().browse(new URI(url.getURL()));
        } catch (URISyntaxException | IOException e) {
            System.out.println("Не удалось открыть ссылку");
        }
    }

    private void stop() {
        this.running = false;
    }

    private boolean unauthorized() {
        return this.currUser == null;
    }
}
