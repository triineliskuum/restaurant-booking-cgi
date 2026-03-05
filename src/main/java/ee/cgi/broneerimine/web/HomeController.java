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

@Controller
public class HomeController {

    private final TableRepository tableRepo;
    private final ReservationRepository reservationRepo;
    private final RecommendationService recommendationService;
    private static final DateTimeFormatter datetimeformat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    public HomeController(TableRepository tableRepo, ReservationRepository reservationRepo, RecommendationService recommendationService) {
        this.tableRepo = tableRepo;
        this.reservationRepo = reservationRepo;
        this.recommendationService = recommendationService;
    }

    @GetMapping("/")
    public String home(@RequestParam(required = false) String dt,
                       @RequestParam(required = false) Integer people,
                       @RequestParam(required = false) Set<Preference> prefs,
                       Model model) {
        int p = (people == null || people < 1) ? 2 : people;
        Set<Preference> selectedPrefs = (prefs == null) ? Set.of() : prefs;

        LocalDateTime time = (dt == null || dt.isBlank())
                ? LocalDateTime.now().withSecond(0).withNano(0)
                : LocalDateTime.parse(dt, datetimeformat);

        LocalDateTime to = time.plusHours(2);

        var reservations = reservationRepo.findByStartTimeLessThanAndEndTimeGreaterThan(to, time);
        Set<Long> busyIds = reservations.stream().map(r -> r.getTableId()).collect(Collectors.toSet());

        var tables = tableRepo.findAll();

        Set<TableEntity> available = tables.stream()
                .filter(t -> !busyIds.contains(t.getId()))
                .sorted(java.util.Comparator.comparing(TableEntity::getId))
                .collect(Collectors.toCollection(java.util.LinkedHashSet::new));

        Long recommendedId = recommendationService
                .recommendBest(available, p, selectedPrefs)
                .map(TableEntity::getId)
                .orElse(null);

        model.addAttribute("tables", tables);
        model.addAttribute("busyIds", busyIds);
        model.addAttribute("recommendedId", recommendedId);

        // et vorm saaks väärtused tagasi
        model.addAttribute("people", p);
        model.addAttribute("prefs", selectedPrefs);

        model.addAttribute("dt", time.format(datetimeformat));

        return "index";
    }

    @PostMapping("/reserve")
    public String reserve(@RequestParam Long tableId,
                          @RequestParam String dt,
                          @RequestParam Integer people,
                          @RequestParam(required = false) Set<Preference> prefs,
                          RedirectAttributes ra) {

        String prefsQuery = "";
        if (prefs != null && !prefs.isEmpty()) {
            prefsQuery = prefs.stream()
                    .map(p -> "&prefs=" + p.name())
                    .collect(Collectors.joining());
        }

        LocalDateTime start = LocalDateTime.parse(dt, datetimeformat);
        LocalDateTime end = start.plusHours(2);

        // Kontroll: kas laud on vaba (kattuvus)
        boolean busy = reservationRepo
                .existsByTableIdAndStartTimeLessThanAndEndTimeGreaterThan(tableId, end, start);

        if (busy) {
            ra.addFlashAttribute("error", "See laud on sellel ajal juba broneeritud.");
            return "redirect:/?dt=" + dt + "&people=" + people + prefsQuery;
        }

        ReservationEntity r = new ReservationEntity();
        r.setTableId(tableId);
        r.setStartTime(start);
        r.setEndTime(end);
        r.setPartySize(people);

        reservationRepo.save(r);

        String label = tableRepo.findById(tableId).map(TableEntity::getLabel).orElse("?");
        ra.addFlashAttribute("success", "Broneering loodud! Laud: " + label);
        return "redirect:/?dt=" + dt + "&people=" + people + prefsQuery;
    }
}