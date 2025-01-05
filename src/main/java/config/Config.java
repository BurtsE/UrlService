package config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/// Класс конфигурации приложения, содержащий лимит переходов по URL
/// и длительность действия ссылки по умолчанию
public class Config {
    public int URLAccessLimit;
    public long URLExpirationTimeMillis;
}
