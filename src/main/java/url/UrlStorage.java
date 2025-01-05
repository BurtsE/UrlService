package url;


public interface UrlStorage {
    boolean AddUrl(Url url);
    boolean RemoveUrl(String url, String UserId);
    Url GetUrl(String url, String UserId);
    Url GetUrlByShortUrl(String ShortUrl, String UserId);
}
