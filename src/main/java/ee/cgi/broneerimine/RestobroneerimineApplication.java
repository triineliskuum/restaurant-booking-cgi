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

@SpringBootApplication
public class RestobroneerimineApplication {

    public static void main(String[] args) {
        SpringApplication.run(RestobroneerimineApplication.class, args);
    }

    @Bean
    CommandLineRunner seed(TableRepository tableRepo, ReservationRepository resRepo) {
        return args -> {
            if (tableRepo.count() == 0) {
                tableRepo.save(makeTable("T1", 2, Zone.SISESAAL, 1, 1, EnumSet.of(Preference.WINDOW)));
                tableRepo.save(makeTable("T2", 4, Zone.SISESAAL, 3, 1, EnumSet.of(Preference.QUIET)));
                tableRepo.save(makeTable("T3", 4, Zone.TERRASS, 1, 3, EnumSet.of(Preference.WINDOW)));
                tableRepo.save(makeTable("T4", 6, Zone.SISESAAL, 3, 3, EnumSet.of(Preference.NEAR_KIDS)));
                tableRepo.save(makeTable("T5", 8, Zone.PRIVAATRUUM, 5, 2, EnumSet.of(Preference.QUIET, Preference.ACCESSIBLE)));
            }

            Random r = new Random();
            var tables = tableRepo.findAll();
            LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);

            // 8 random broneeringut, kestus 2h
            for (int i = 0; i < 8; i++) {
                var t = tables.get(r.nextInt(tables.size()));

                int startPlusMinutes = r.nextInt(12) * 60; // 0..11h
                LocalDateTime start = now.plusMinutes(startPlusMinutes);
                LocalDateTime end = start.plusHours(2);

                ReservationEntity res = new ReservationEntity();
                res.setTableId(t.getId());
                res.setStartTime(start);
                res.setEndTime(end);
                res.setPartySize(Math.min(t.getCapacity(), 2 + r.nextInt(4)));

                resRepo.save(res);
            }
        };
    }

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
