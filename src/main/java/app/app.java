package app;

import Menu.Menu;
import com.google.gson.Gson;
import config.Config;
import url.Storage;
import user.DiskStorage;
import user.User;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;


public class app {

    private Menu menu;
    private User currUser;

    public app() {
        Config config = this.config();
        if (config == null) {
            return;
        }
        menu = new Menu(new DiskStorage(), new Storage(), System.in, config);
    }

    public void Run() {
        menu.run();
    }

    private Config config() {
        Gson gson = new Gson();
        Config config = null;
        try (Reader reader = new FileReader("configs/config.json")) {
            config = gson.fromJson(reader, Config.class);
        } catch (IOException e) {
            System.out.println("could not load config");;
        }
        return config;
    }
}

