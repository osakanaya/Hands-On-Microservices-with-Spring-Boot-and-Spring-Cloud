package se.magnus.microservices.composite.product;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@Component
public class ReviewHealthIndicator extends AbstractHealthIndicator {

	private final WebClient.Builder webClientBuilder;

	private WebClient webClient;
	
	@Autowired
	public ReviewHealthIndicator(
		WebClient.Builder webClientBuilder
	) {
		this.webClientBuilder = webClientBuilder;
	}
	
	@Override
	protected WebClient getWebClient() {
		if (webClient == null) {
			webClient = webClientBuilder.build();
		}
		
		return webClient;
	}

	@Override
	public Mono<Health> health() {
		return getHealth("http://review");
	}
}
