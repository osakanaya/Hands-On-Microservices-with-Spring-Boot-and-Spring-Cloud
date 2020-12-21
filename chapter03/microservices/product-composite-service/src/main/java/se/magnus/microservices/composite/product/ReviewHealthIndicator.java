package se.magnus.microservices.composite.product;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@Component
public class ReviewHealthIndicator extends AbstractHealthIndicator {

	private final WebClient webClient;
	
	private final String reviewServiceUrl;

	@Autowired
	public ReviewHealthIndicator(
		WebClient.Builder webClient,
		@Value("${app.review-service.host}")
		String reviewServiceHost,
		@Value("${app.review-service.port}")
		int reviewServicePort
	) {
		this.webClient = webClient.build();
		this.reviewServiceUrl = "http://" + reviewServiceHost + ":" + reviewServicePort;
	}
	
	@Override
	protected WebClient getWebClient() {
		return webClient;
	}

	@Override
	public Mono<Health> health() {
		return getHealth(reviewServiceUrl);
	}
}
