package it.blog.reactive.mvc;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ReactiveHttpOutputMessage;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.ModelAndView;

import reactor.core.publisher.Mono;
import static java.util.Comparator.comparing;

@Controller
public class BookingController {

	private static final Logger logger = LoggerFactory.getLogger(BookingController.class);

	private final WebClient daoClient = WebClient.create("http://localhost:8080");

	@GetMapping("/")
	public ModelAndView welcome(Model model) {
		logger.info("Start Request for welcome v1.6");
		model.addAttribute("booking", new Booking());

		return new ModelAndView("home");
	}

	@GetMapping("/bookings")
	public ModelAndView getBooking() {

		logger.info("Start Request for Bookings");

		List<Booking> booking = daoClient
				.get()
				.uri("/list")
				.retrieve()
				.bodyToFlux(Booking.class)
				.sort(comparing(Booking::getSurname))
				.doOnError(e -> logger.error("Error:", e))
				.collectList()
				.log()
				.block();

		ModelAndView model = new ModelAndView("list");
		model.addObject("bookings", booking);
		return model;

	}

	@PostMapping("/add")
	private ModelAndView addBooking(@ModelAttribute Booking booking) {

		BodyInserter<Object, ReactiveHttpOutputMessage> inserterBooking = BodyInserters.fromObject(booking);

		daoClient
			.post()
			.uri("/add")
			.body(inserterBooking)
			.retrieve()
			.bodyToMono(Void.class)
			.log()
			.block();

		logger.info("Item added");

		return new ModelAndView("redirect:/");
	}
}