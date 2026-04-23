package api.generators;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.concurrent.ThreadLocalRandom;

public class RandomData {
    private RandomData() {}

    public static String getUsername() {
        return RandomStringUtils.randomAlphabetic(5);
    }

    public static String getPassword() {
        return RandomStringUtils.randomAlphabetic(3).toUpperCase() +
                RandomStringUtils.randomAlphabetic(5).toLowerCase() +
                RandomStringUtils.randomNumeric(3) + "%$#";
    }

    public static String getName() {
        return generateWordWithFirstCapital(5) + " " + generateWordWithFirstCapital(5);
    }

    private static String generateWordWithFirstCapital(int length) {
        StringBuilder sb = new StringBuilder(length);
        sb.append((char) ('A' + ThreadLocalRandom.current().nextInt(26)));
        for (int i = 1; i < length; i++) {
            sb.append((char) ('a' + ThreadLocalRandom.current().nextInt(26)));
        }
        return sb.toString();
    }

    public static double getRandomValidDepositAmount() {
        double min = 0.01;
        double max = 5000.0;

        double value = ThreadLocalRandom.current().nextDouble(min, max);

        return Math.round(value * 100.0) / 100.0;
    }

    public static double getRandomValidTransferLessOrEqualDeposit(double depositAmount) {
        double min = 0.01;
        double max = depositAmount;

        double value = ThreadLocalRandom.current().nextDouble(min, max);

        return Math.round(value * 100.0) / 100.0;
    }

    public static double getRandomTransferMoreThanDeposit(double depositAmount) {
        double min = depositAmount + 0.01;
        double max = 10000.0;

        double value = ThreadLocalRandom.current().nextDouble(min, max);

        return Math.round(value * 100.0) / 100.0;
    }

    public static String getRandomInvalidName() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            sb.append((char) ('a' + ThreadLocalRandom.current().nextInt(26)));
        }
        return sb.toString();
    }
}
