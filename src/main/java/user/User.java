package user;

import java.io.Serializable;

public class User implements Serializable {
    private final String UUID;
    public User(String uuid) {
        this.UUID = uuid;
    }
    public String getID() {
        return this.UUID;
    }
}
