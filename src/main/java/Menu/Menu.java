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


/// Необходим для отложенного определения функций MenuItem
interface Option {
    void execute();
}
/// В классе Menu определена основная логика программы. Каждая
/// функция определена объектом класса MenuItem.
public class Menu {
    private final String dateFormat = "dd/MM/yyyy hh:mm:ss";
    private final Scanner scanner;
    private final Config cfg;
    private final UserStorage userStorage;
    private final UrlStorage urlStorage;
    private boolean running = true;
    private User currUser;

    /// Класс определяет функциональность пункта меню,
    /// которую получает в конструкторе
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

    /// Инициализация класса Menu, создание основных пунктов
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
    // Основной цикл программы
    public void run() {
        while (running) {
            show();
            Integer option = getOption();
            options.get(option - 1).onSelected.execute();
        }
    }
    // Считывание нужного пункта меню
    private Integer getOption() {
        Integer option = null;
        while (option == null || option < 1 || option > this.options.size() + 1) {
            try {
                option = scanner.nextInt();
            } catch (InputMismatchException e) {
                System.out.println("Неверный формат ввода");
            }
            scanner.nextLine();
        }
        return option;
    }
    // Отрисовка меню
    private void show() {
        for (MenuItem option : options) {
            System.out.println(option.getName());
        }
    }
    // Функция авторизации/аутентификации
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
                System.out.println("Аутентификация не удалась");
            } else {
                System.out.println("Вы авторизованы как " + currUser.getID());
            }
        } while (currUser == null);
    }
    /// Функция добавления ссылки. Пользователь может определить срок действия ссылки и количество переходов,
    /// либо оставить значения по умолчанию. Если ссылка существует, она будет удалена и заменена на новую,
    /// таким образом функциональность удаления и обновления ссылок идентичны.
    ///
    /// При создании ссылки в качестве соли для хэша используется уникальный идентификатор пользователя, поэтому
    /// для каждого пользователя создаётся своя ссылка. При вызове с одними и теми же параметрами ссылки также
    /// будут различны (см. URLShortener)
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
    // Функция удаления ссылки
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
    // Удаление ссылки (см. функцию добавления ссылки)
    private void updateUrl() {
        this.addUrl();
    }
    /// Открытие ссылки. Если лимит переходов исчерпан, либо ссылка просрочена, пользователь будет уведомлен об этом,
    /// а ссылка удалена из хранилища.
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
    // Закрытие приложения
    private void stop() {
        this.running = false;
    }
    // Проверка авторизации
    private boolean unauthorized() {
        return this.currUser == null;
    }
}
