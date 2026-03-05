package ee.cgi.broneerimine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import ee.cgi.broneerimine.domain.*;
import ee.cgi.broneerimine.repo.ReservationRepository;
import ee.cgi.broneerimine.repo.TableRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

/**
 * Rakenduse käivitamise klass:
 * - käivitab Spring Boot rakenduse
 * - loob demoandmed (lauad ja juhuslikud broneeringud) kui andmebaas on tühi
 * Seed-andmed võimaldavad rakendust kohe testida, ilma et kasutaja peaks käsitsi laudu või broneeringuid looma.
 */
@SpringBootApplication
public class RestobroneerimineApplication {

    public static void main(String[] args) {
        SpringApplication.run(RestobroneerimineApplication.class, args);
    }

    /**
     * CommandLineRunner käivitatakse automaatselt pärast rakenduse starti.
     * Kasutatakse demoandmete loomiseks:
     * - lisatakse mõned näidislauad
     * - genereeritakse juhuslikud broneeringud
     * Andmeid lisatakse ainult siis, kui tabel on tühi.
     */
    @Bean
    CommandLineRunner seed(TableRepository tableRepo, ReservationRepository resRepo) {
        return args -> {
            if (tableRepo.count() == 0) {
                tableRepo.save(makeTable("T1", 2, Zone.SISESAAL, 1, 1, EnumSet.of(Preference.AKNAALL)));
                tableRepo.save(makeTable("T2", 4, Zone.SISESAAL, 3, 1, EnumSet.of(Preference.VAIKNENURK)));
                tableRepo.save(makeTable("T3", 4, Zone.TERRASS, 1, 3, EnumSet.of(Preference.AKNAALL)));
                tableRepo.save(makeTable("T4", 6, Zone.SISESAAL, 3, 3, EnumSet.of(Preference.LASTENURGALAHEDAL)));
                tableRepo.save(makeTable("T5", 8, Zone.PRIVAATRUUM, 5, 2, EnumSet.of(Preference.VAIKNENURK, Preference.LIHTNELIGIPAAS)));
            }

            // Random objekt juhuslike broneeringute loomiseks
            Random r = new Random();

            // Kõikide laudade laadimine
            var tables = tableRepo.findAll();
            LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);

            // Luuakse 8 juhuslikku broneeringut, iga broneering kestab 2 tundi
            for (int i = 0; i < 8; i++) {
                // Valitakse juhuslik laud
                var t = tables.get(r.nextInt(tables.size()));

                // Broneeringu algusaeg on 0–11 tunni jooksul praegusest ajast
                int startPlusMinutes = r.nextInt(12) * 60;
                LocalDateTime start = now.plusMinutes(startPlusMinutes);
                LocalDateTime end = start.plusHours(2);

                ReservationEntity res = new ReservationEntity();
                res.setTableId(t.getId());
                res.setStartTime(start);
                res.setEndTime(end);

                // Inimeste arv (väldib laua mahutavuse ületamist)
                res.setPartySize(Math.min(t.getCapacity(), 2 + r.nextInt(4)));

                // Broneeringu salvestamine andmebaasi
                resRepo.save(res);
            }
        };
    }

    /**
     * Abimeetod uue TableEntity objekti loomiseks.
     * @param label laua nimi (nt T1, T2)
     * @param cap laua mahutavus
     * @param zone millises tsoonis laud asub
     * @param x laua x-koordinaat saaliplaanil
     * @param y laua y-koordinaat saaliplaanil
     * @param features laua omadused / eelistused
     * @return täidetud TableEntity objekt
     */
    private TableEntity makeTable(String label, int cap, Zone zone, int x, int y, Set<Preference> features) {
        TableEntity t = new TableEntity();
        t.setLabel(label);
        t.setCapacity(cap);
        t.setZone(zone);
        t.setX(x);
        t.setY(y);
        t.setFeatures(features);
        return t;
    }
}
