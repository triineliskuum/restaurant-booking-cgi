package ee.cgi.broneerimine.service;

import ee.cgi.broneerimine.domain.Preference;
import ee.cgi.broneerimine.domain.TableEntity;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.Optional;
import java.util.Set;

/**
 * RecommendationService vastutab sobivaima laua valimise eest.
 * Teenus võtab arvesse:
 * - inimeste arvu
 * - kasutaja valitud eelistusi
 * - laua suurust
 * Iga laua jaoks arvutatakse punktisumma, mille põhjal valitakse kõige kõrgema väärtusega laud.
 */
@Service
public class RecommendationService {

    /**
     * Leiab parima laua saadaval olevate laudade hulgast.
     * @param availableTables kõik lauad, mis ei ole valitud ajal broneeritud
     * @param people inimeste arv
     * @param prefs kasutaja valitud eelistused
     */
    public Optional<TableEntity> recommendBest(Set<TableEntity> availableTables,
                                               int people,
                                               Set<Preference> prefs) {

        return availableTables.stream()
                // eemaldatakse lauad, kuhu inimesed ära ei mahu
                .filter(t -> t.getCapacity() >= people)
                // valitakse laud, mille punktisumma on kõige suurem
                .max(Comparator.comparingDouble(t -> score(t, people, prefs)));
    }

    /**
     * Arvutab laua punktisumma, mille põhjal otsustatakse kui sobiv laud on.
     * Punktisumma koosneb kolmest komponendist:
     * 1. laua suuruse sobivus
     * 2. kasutaja eelistuste vastavus
     * 3. boonus täpse suuruse eest
     */
    private double score(TableEntity t, int people, Set<Preference> prefs) {
        // Arvutatakse, mitu kohta jääb lauas üle
        int extraSeats = t.getCapacity() - people;

        // 1) Suuruse sobivus – mida vähem ülejäävaid kohti, seda parem (väldib liiga suuri laudu)
        double sizeScore = 100 - (extraSeats * 10);

        // 2) Eelistused – iga kasutaja eelistus, mis laual olemas on, annab lisapunkte
        long matches = prefs.stream().filter(p -> t.getFeatures().contains(p)).count();
        double prefScore = matches * 25;

        // 3) Boonus täpse suuruse eest – kui laud mahutab täpselt õige arvu inimesi, lisatakse boonuspunkte
        double exactBonus = (extraSeats == 0) ? 15 : 0;

        // Lõplik punktisumma
        return sizeScore + prefScore + exactBonus;
    }
}