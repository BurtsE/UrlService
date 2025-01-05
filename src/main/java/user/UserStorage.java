package user;

public interface UserStorage {
    public User Create();
    public User Get(String id);
    public boolean Delete(User user);
}
