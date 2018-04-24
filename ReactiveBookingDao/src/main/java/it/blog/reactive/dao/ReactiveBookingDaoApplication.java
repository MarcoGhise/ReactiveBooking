package it.blog.reactive.dao;

import java.time.Duration;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoOperations;

import reactor.core.publisher.Flux;

@SpringBootApplication
public class ReactiveBookingDaoApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReactiveBookingDaoApplication.class, args);
	}
	
	@Bean
	public CommandLineRunner initData(MongoOperations mongo) {
		return (String... args) -> {

			mongo.dropCollection(Booking.class);
			mongo.createCollection(Booking.class);
			
			Flux.range(1, 10)
					.map(i -> new Booking(i))
					.doOnNext(mongo::save)
					.blockLast(Duration.ofSeconds(5));
		};
	}
}
