package lab1;

import java.util.Locale;

public final class Lab2Support {

    private Lab2Support() {
    }

    public static String titleFromFileName(String fileName) {
        String title = fileName;
        int extensionStart = title.toLowerCase(Locale.ROOT).lastIndexOf(".txt");
        if (extensionStart >= 0) {
            title = title.substring(0, extensionStart);
        }
        return title.replace('_', ' ').trim().toLowerCase(Locale.ROOT);
    }

    public static float boostToMatchScore(float targetScore, float baseScore) {
        if (baseScore <= 0.0f) {
            throw new IllegalArgumentException("Base score must be greater than zero.");
        }
        return targetScore / baseScore;
    }
}
