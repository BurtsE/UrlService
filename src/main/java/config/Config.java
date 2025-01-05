package config;

import java.io.*;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/// Класс конфигурации приложения, содержащий лимит переходов по URL
/// и длительность действия ссылки по умолчанию
public class Config {
    public int URLAccessLimit;
    public long URLExpirationTimeMillis;
}
