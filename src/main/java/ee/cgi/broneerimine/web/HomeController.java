package ee.cgi.broneerimine.web;

import ee.cgi.broneerimine.domain.Preference;
import ee.cgi.broneerimine.domain.ReservationEntity;
import ee.cgi.broneerimine.domain.TableEntity;
import ee.cgi.broneerimine.repo.ReservationRepository;
import ee.cgi.broneerimine.repo.TableRepository;
import ee.cgi.broneerimine.service.RecommendationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * HomeController vastutab peamise vaate (saaliplaan) kuvamise eest ning uute broneeringute loomise eest.
 * Controller kogub kasutaja sisendi (aeg, inimeste arv, eelistused), kontrollib laudade saadavust ning kasutab RecommendationService'i,
 * et leida sobivaim laud.
 */
@Controller
public class HomeController {

    // Repository tabelite andmete lugemiseks
    private final TableRepository tableRepo;

    // Repository broneeringute lugemiseks ja salvestamiseks
    private final ReservationRepository reservationRepo;

    // Klass sobivaima laua valimiseks kasutaja eelistuste põhjal
    private final RecommendationService recommendationService;

    // Vormis kasutatav kuupäeva ja kellaaja formaat
    private static final DateTimeFormatter datetimeformat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    public HomeController(TableRepository tableRepo, ReservationRepository reservationRepo, RecommendationService recommendationService) {
        this.tableRepo = tableRepo;
        this.reservationRepo = reservationRepo;
        this.recommendationService = recommendationService;
    }

    /**
     * Kuvab saaliplaani ja arvutab:
     * - millised lauad on hõivatud
     * - millised lauad on saadaval
     * - milline laud on soovitatav kasutaja sisendi põhjal
     */
    @GetMapping("/")
    public String home(@RequestParam(required = false) String dt,
                       @RequestParam(required = false) Integer people,
                       @RequestParam(required = false) Set<Preference> prefs,
                       Model model) {

        // Kui inimeste arvu pole sisestatud või see on vigane, kasutatakse vaikimisi väärtust 2
        int p = (people == null || people < 1) ? 2 : people;

        // Kui eelistusi pole valitud, kasutatakse tühja hulka
        Set<Preference> selectedPrefs = (prefs == null) ? Set.of() : prefs;

        // Kui aega pole määratud, kasutatakse praegust aega, muidu parsitakse aeg vormist
        LocalDateTime time = (dt == null || dt.isBlank())
                ? LocalDateTime.now().withSecond(0).withNano(0)
                : LocalDateTime.parse(dt, datetimeformat);

        // Broneeringu kestus on 2 tundi
        LocalDateTime to = time.plusHours(2);

        // Leitakse kõik broneeringud, mis kattuvad valitud ajaga
        var reservations = reservationRepo.findByStartTimeLessThanAndEndTimeGreaterThan(to, time);

        // Moodustatakse hõivatud laudade ID-de hulk
        Set<Long> busyIds = reservations.stream().map(r -> r.getTableId()).collect(Collectors.toSet());

        // Andmebaasist laetakse kõik lauad
        var tables = tableRepo.findAll();

        // Filtreeritakse välja ainult need lauad, mis ei ole hõivatud ning sorteeritakse lauad ID järgi
        Set<TableEntity> available = tables.stream()
                .filter(t -> !busyIds.contains(t.getId()))
                .sorted(java.util.Comparator.comparing(TableEntity::getId))
                .collect(Collectors.toCollection(java.util.LinkedHashSet::new));

        // Sobivaima laua leidmiseks kasutatakse klassi RecommendationService
        Long recommendedId = recommendationService
                .recommendBest(available, p, selectedPrefs)
                .map(TableEntity::getId)
                .orElse(null);

        // Vajalike andmete vaatesse edastamine
        model.addAttribute("tables", tables);
        model.addAttribute("busyIds", busyIds);
        model.addAttribute("recommendedId", recommendedId);

        // Vormi väärtuste tagastamine, et kasutaja sisend säiliks
        model.addAttribute("people", p);
        model.addAttribute("prefs", selectedPrefs);

        model.addAttribute("dt", time.format(datetimeformat));

        return "index";
    }

    /**
     * Kontrollib:
     * - kas laud on valitud ajal saadaval
     * - kui on, salvestab broneeringu
     * - kuvab kasutajale success või error teate
     */
    @PostMapping("/reserve")
    public String reserve(@RequestParam Long tableId,
                          @RequestParam String dt,
                          @RequestParam Integer people,
                          @RequestParam(required = false) Set<Preference> prefs,
                          RedirectAttributes ra) {

        // URL-i parameetrite koostamine, et redirect säilitaks kasutaja valitud eelistused
        String prefsQuery = "";
        if (prefs != null && !prefs.isEmpty()) {
            prefsQuery = prefs.stream()
                    .map(p -> "&prefs=" + p.name())
                    .collect(Collectors.joining());
        }

        LocalDateTime start = LocalDateTime.parse(dt, datetimeformat);
        LocalDateTime end = start.plusHours(2);

        // Kontroll: kas laud on valitud ajal juba broneeritud
        boolean busy = reservationRepo
                .existsByTableIdAndStartTimeLessThanAndEndTimeGreaterThan(tableId, end, start);

        // Kui laud on hõivatud, suunatakse kasutaja tagasi ja kuvatakse veateade
        if (busy) {
            ra.addFlashAttribute("error", "See laud on sellel ajal juba broneeritud.");
            return "redirect:/?dt=" + dt + "&people=" + people + prefsQuery;
        }

        // Luuakse uus broneeringu objekt
        ReservationEntity r = new ReservationEntity();
        r.setTableId(tableId);
        r.setStartTime(start);
        r.setEndTime(end);
        r.setPartySize(people);

        // Broneeringu salvestamine andmebaasi
        reservationRepo.save(r);

        // Laua nime (nt T1, T2 jne) leidmine kasutajale kuvamiseks
        String label = tableRepo.findById(tableId).map(TableEntity::getLabel).orElse("?");

        // Kasutajale success-teate kuvamine
        ra.addFlashAttribute("success", "Broneering loodud! Laud: " + label);

        // Kasutaja suunamine tagasi avalehele
        return "redirect:/?dt=" + dt + "&people=" + people + prefsQuery;
    }
}