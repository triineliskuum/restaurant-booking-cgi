package ee.cgi.broneerimine.web;

import ee.cgi.broneerimine.domain.Preference;
import ee.cgi.broneerimine.domain.TableEntity;
import ee.cgi.broneerimine.repo.ReservationRepository;
import ee.cgi.broneerimine.repo.TableRepository;
import ee.cgi.broneerimine.service.RecommendationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    private final TableRepository tableRepo;
    private final ReservationRepository reservationRepo;
    private final RecommendationService recommendationService;

    public HomeController(TableRepository tableRepo, ReservationRepository reservationRepo, RecommendationService recommendationService) {
        this.tableRepo = tableRepo;
        this.reservationRepo = reservationRepo;
        this.recommendationService = recommendationService;
    }

    @GetMapping("/")
    public String home(@RequestParam(required = false) Integer people,
                       @RequestParam(required = false) Set<Preference> prefs,
                       Model model) {
        int p = (people == null || people < 1) ? 2 : people;
        Set<Preference> selectedPrefs = (prefs == null) ? Set.of() : prefs;

        LocalDateTime time = LocalDateTime.now().withSecond(0).withNano(0);
        LocalDateTime to = time.plusHours(2);

        var reservations = reservationRepo.findByStartTimeLessThanAndEndTimeGreaterThan(to, time);
        Set<Long> busyIds = reservations.stream().map(r -> r.getTableId()).collect(Collectors.toSet());

        var tables = tableRepo.findAll();

        Set<TableEntity> available = tables.stream()
                .filter(t -> !busyIds.contains(t.getId()))
                .collect(Collectors.toSet());

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

        return "index";
    }
}