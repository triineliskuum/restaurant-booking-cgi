package ee.cgi.broneerimine.service;

import ee.cgi.broneerimine.domain.Preference;
import ee.cgi.broneerimine.domain.TableEntity;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.Optional;
import java.util.Set;

@Service
public class RecommendationService {

    public Optional<TableEntity> recommendBest(Set<TableEntity> availableTables,
                                               int people,
                                               Set<Preference> prefs) {

        return availableTables.stream()
                .filter(t -> t.getCapacity() >= people)
                .max(Comparator.comparingDouble(t -> score(t, people, prefs)));
    }

    private double score(TableEntity t, int people, Set<Preference> prefs) {
        int extraSeats = t.getCapacity() - people;

        // 1) suuruse sobivus (mida vähem üle, seda parem)
        double sizeScore = 100 - (extraSeats * 10);

        // 2) eelistused (iga match annab punkte)
        long matches = prefs.stream().filter(p -> t.getFeatures().contains(p)).count();
        double prefScore = matches * 25;

        // 3) boonus täpse suuruse eest
        double exactBonus = (extraSeats == 0) ? 15 : 0;

        return sizeScore + prefScore + exactBonus;
    }
}