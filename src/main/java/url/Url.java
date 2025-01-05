package url;

import java.util.Date;

public class Url {

    String URL;
    String UserId;
    String ShortURL;
    Date CreatedAt;
    Date ExpiresAt;
    int accessLimit;

    public Url(String LongURL, String ShortURL, String UserId, Date ExpirationDate, int accessLimit) {
        this.URL = LongURL;
        this.ShortURL = ShortURL;
        this.UserId = UserId;
        this.CreatedAt = new Date();
        this.ExpiresAt = ExpirationDate;
        this.accessLimit = accessLimit;
    }

    public Date getExpirationDate() {
        return ExpiresAt;
    }

    public void setExpirationDate(Date expirationDate) {
        ExpiresAt = expirationDate;
    }

    public String getURL() {
        return URL;
    }

    public String getUserId() {
        return UserId;
    }

    public String getShortURL() {
        return ShortURL;
    }

    public Date getCreatedAt() {
        return CreatedAt;
    }

    public Date getExpiresAt() {
        return ExpiresAt;
    }

    public int getAccessLimit() {
        return accessLimit;
    }

    public boolean isExpired() {
        return ExpiresAt.before(new Date());
    }

    public boolean accessLimitReached() {
        return accessLimit < 0;
    }
}
