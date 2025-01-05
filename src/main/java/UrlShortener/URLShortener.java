package UrlShortener;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Random;

/// Класс для создания ссылок. Создание происходит на основе url соли, что обеспечивает возможность генерации
/// различных ссылок для разных пользователей. Изменение случайного символа позволяет сохдавать новые ссылки для
/// одного и того же пользователя
public class URLShortener {
    private static final String BASE62 = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final Random RANDOM = new Random();

    public static String GenerateShortURL(String url, String salt) {
        try {
            // Хэшируем при помощи алгоритма PBKDF2
            KeySpec spec = new PBEKeySpec(url.toCharArray(), salt.getBytes(), 65536, 128);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] hash = factory.generateSecret(spec).getEncoded();

            // Преобразуем хэш в base62
            BigInteger bigInt = new BigInteger(1, hash);

            // преобразуем BigInteger в строку base62
            StringBuilder base62 = new StringBuilder();
            while (bigInt.compareTo(BigInteger.ZERO) > 0) {
                BigInteger[] divmod = bigInt.divideAndRemainder(new BigInteger(String.valueOf(BASE62.length())));
                base62.append(BASE62.charAt(divmod[1].intValue()));
                bigInt = divmod[0];
            }

            // Добавляем случайный символ к строке
            int randomNumber = RANDOM.nextInt(BASE62.length());
            base62.append(BASE62.charAt(randomNumber));
            return base62.reverse().toString();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            System.out.println("Ошибка при создании ссылки");
            return null;
        }
    }
}
