package url;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/// UrlStorage хранит значения ссылок в словаре, где ключами являются идентификаторы пользователей
///
/// Класс не защищен от повторных вхождений
public class Storage implements UrlStorage {
    private final  Map<String, List<Url>> urls;


    public Storage() {
        this.urls = new HashMap<>();
    }
    @Override
    public boolean AddUrl(Url url) {
        List<Url> list = urls.computeIfAbsent(url.UserId, _ -> new ArrayList<>());
        return list.add(url);
    }


    @Override
    public Url GetUrl(String url, String UserId) {
        List<Url> list = urls.get(UserId);
        if (list == null) {
            return null;
        }
        for (Url u : list) {
            if (u.URL.equals(url)) {
                return u;
            }
        }
        return null;
    }

    @Override
    public boolean RemoveUrl(String url, String UserId) {
        List<Url> list = urls.get(UserId);
        if (list == null) {
            return true;
        }
        return list.removeIf(u -> u.URL.equals(url));
    }
    @Override
    public Url GetUrlByShortUrl(String ShortUrl, String UserId) {
        List<Url> list = urls.get(UserId);
        if (list == null) {
            return null;
        }
        for (Url u : list) {
            if (u.getShortURL().equals(ShortUrl)) {
                u.accessLimit--;
                return u;
            }
        }
        return null;
    }
}
