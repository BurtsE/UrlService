package user;

import java.io.*;
import java.util.UUID;

class Constants {
    public static final String USER_DIR = "Users";
}

/// Класс реализует сохранение и загрузку пользователей с дискового хранилища
/// Для работы необходима папка Users в корне проекта
public class DiskStorage implements UserStorage {
    @Override
    public User Create() {
        UUID uuid = UUID.randomUUID();
        User user = new User(uuid.toString());
        saveUser(user);
        return user;
    }

    @Override
    public boolean Delete(User user) {
        File file = new File(formatFilename(user.getID()));
        return file.delete();
    }

    @Override
    public User Get(String UUID) {
        User user = null;
        try (
                FileInputStream fileIn = new FileInputStream(formatFilename(UUID));
                ObjectInputStream in = new ObjectInputStream(fileIn)
        ) {
            user = (User) in.readObject();
        } catch (IOException e) {
            System.out.println("User not found");
            return null;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return user;
    }

    private void saveUser(User user) {
        try (
                FileOutputStream fileOut = new FileOutputStream(formatFilename(user.getID()));
                ObjectOutputStream out = new ObjectOutputStream(fileOut);
        ) {
            out.writeObject(user);
        } catch (IOException e) {
            System.out.println("Could not save user");
        }
    }
    private String formatFilename(String userID) {
        return Constants.USER_DIR + "/" + userID + ".txt";
    }
}
