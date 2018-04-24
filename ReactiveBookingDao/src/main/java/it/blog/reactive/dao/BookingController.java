/*
 * Copyright 2002-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package it.blog.reactive.dao;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BookingController {

	private final BookingRepository repository;

	private static final Logger logger = LoggerFactory.getLogger(BookingController.class);

	public BookingController(BookingRepository repository) {
		this.repository = repository;
	}

	@GetMapping("/list")
	public Flux<Booking> getBooking() {

		logger.info("Get Bookings");

		return this.repository.findAll().log();
	}

	/*
	 * curl -H "Accept: application/json" -H "Content-Type:application/json" -X POST -d '{"code":"101", "flightNumber":"Flight-101", "name":"Name-101", "surname":"Surname-101", "seat":"Seat-101"}' http://localhost:8080/add
	 */
	@PostMapping(path = "/add")
	@ResponseStatus(HttpStatus.CREATED)
	public Mono<Void> addBooking(@RequestBody Flux<Booking> booking) {
		
		return this.repository.insert(booking).log().then();
	}
}