package it.blog.reactive;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.WebClient;

import it.blog.reactive.mvc.BookingController;
import it.blog.reactive.mvc.ReactiveBookingMvcApplication;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


import static java.util.stream.Collectors.joining;

import java.time.Duration;

import static java.util.Comparator.comparing;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = ReactiveBookingMvcApplication.class)
public class ReactiveBookingMvcApplicationTests {

	private static final Logger logger = LoggerFactory.getLogger(ReactiveBookingMvcApplicationTests.class);

	@Test
	@Ignore
	public void webClientTest() {
		Flux<String> response = WebClient.create("http://www.virgilio.it").get().uri("/").retrieve()
				.bodyToFlux(String.class);

		System.out.println(response.toString());
	}

	@Test
	@Ignore
	public void StreamTest() throws InterruptedException {

		final Flux<ServerSentEvent> stream = WebClient.create("http://emojitrack-gostreamer.herokuapp.com").get()
				.uri("/subscribe/eps").retrieve().bodyToFlux(ServerSentEvent.class);

		stream.subscribe(sse -> logger.info("Received: {}", sse));

		TimeUnit.MINUTES.sleep(1);

	}

	@Test
	public void StreamTestTransformation() throws InterruptedException {
		
		final Flux<String> stream = WebClient
				/*
				 * Host
				 */
				.create("http://emojitrack-gostreamer.herokuapp.com")
				/*
				 * Http Method
				 */
				.get()
				/*
				 * Uri
				 */
				.uri("/subscribe/eps")
				/*
				 * Get the content
				 */
				.retrieve()
				/*
				 * Convert the result into Flux<ServerSentEvent>
				 */
				.bodyToFlux(ServerSentEvent.class)
				/*
				 * {"1F604":1,"267B":2}
				 */
				.flatMap(e -> Mono.justOrEmpty(e.data()))
				/*
				 * {"1F604":1} {"267B":2}
				 */
				.map(x -> (Map<String, Integer>) x)
				/*
				 * {key="1F604":value=1} {key="267B":value=2}
				 */
				.flatMapIterable(Map::entrySet)
				/*
				 * {1F604} {267B}
				 */
				.flatMap(entry -> Flux.just(entry.getKey()).repeat(entry.getValue()))
				/*
				 * {1F604} {267B} {267B}
				 */
				.map(this::codeToEmoji)
				/*
				 * {🎥}{😍}{😍} 
				 */
				.scan(new HashMap<String, Long>(), (histogram, emoji) -> {
			            histogram.merge(emoji, 1L, Math::addExact);
			            return histogram;
			       })
				/*
				 * {🎥}=1
				 * {🎥}=1,{😍}=1
				 * {🎥}=1,{😍}=2
				 */
				.map(hist -> topEmojis(hist, 50))
				/*
				 * Top 50!
				 * {🎥},{😍}
				 */
				.sample(Duration.ofMillis(100));


		stream.subscribe(sse -> logger.info("Received: {}", sse));
		TimeUnit.SECONDS.sleep(10);

	}

	String topEmojis(HashMap<String, Long> histogram, int max) {
		 return histogram
				 .entrySet()
				 .stream()
				 .sorted(comparing(Map.Entry<String, Long>::getValue).reversed())
				 .limit(max)
				 .map(Map.Entry::getKey)
				 .collect(joining(" "));
	}

	
	private String hexToEmoji(String hex) {
		return new String(Character.toChars(Integer.parseInt(hex, 16)));
	}
	
	private String codeToEmoji(String hex) {
		final String[] codes = hex.split("-");
		if (codes.length == 2) {
			return hexToEmoji(codes[0]) + hexToEmoji(codes[1]);
		} else {
			return hexToEmoji(hex);
		}
	}


}
