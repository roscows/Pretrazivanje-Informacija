package lab1;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Lab2SupportTest {

    @Test
    void derivesTitleFromTextFileName() {
        assertEquals("moby dick", Lab2Support.titleFromFileName("moby_dick.txt"));
        assertEquals("war and peace", Lab2Support.titleFromFileName("war_and_peace.txt"));
        assertEquals("sherlock holmes", Lab2Support.titleFromFileName("sherlock_holmes.TXT"));
    }

    @Test
    void calculatesBoostNeededToReachTargetScore() {
        assertEquals(2.5f, Lab2Support.boostToMatchScore(10.0f, 4.0f), 0.0001f);
    }

    @Test
    void rejectsBoostCalculationWhenBaseScoreIsZero() {
        assertThrows(IllegalArgumentException.class, () -> Lab2Support.boostToMatchScore(1.0f, 0.0f));
    }
}
