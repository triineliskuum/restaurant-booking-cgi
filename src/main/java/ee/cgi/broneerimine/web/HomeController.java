package ee.cgi.broneerimine.web;

import ee.cgi.broneerimine.repo.ReservationRepository;
import ee.cgi.broneerimine.repo.TableRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    private final TableRepository tableRepo;
    private final ReservationRepository reservationRepo;

    public HomeController(TableRepository tableRepo, ReservationRepository reservationRepo) {
        this.tableRepo = tableRepo;
        this.reservationRepo = reservationRepo;
    }

    @GetMapping("/")
    public String home(Model model) {
        LocalDateTime time = LocalDateTime.now().withSecond(0).withNano(0);
        LocalDateTime to = time.plusHours(2);

        var reservations = reservationRepo.findByStartTimeLessThanAndEndTimeGreaterThan(to, time);
        Set<Long> busyIds = reservations.stream().map(r -> r.getTableId()).collect(Collectors.toSet());

        model.addAttribute("tables", tableRepo.findAll());
        model.addAttribute("busyIds", busyIds);

        return "index";
    }
}