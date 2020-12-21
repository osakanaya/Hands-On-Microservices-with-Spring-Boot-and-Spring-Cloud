package se.magnus.microservices.composite.product;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@Component
public class RecommendationHealthIndicator extends AbstractHealthIndicator {

	private final WebClient webClient;
	
	private final String recommendationServiceUrl;

	@Autowired
	public RecommendationHealthIndicator(
		WebClient.Builder webClient,
		@Value("${app.recommendation-service.host}")
		String recommendationServiceHost,
		@Value("${app.recommendation-service.port}")
		int recommendationServicePort
	) {
		this.webClient = webClient.build();
		this.recommendationServiceUrl = "http://" + recommendationServiceHost + ":" + recommendationServicePort;
	}
	
	@Override
	protected WebClient getWebClient() {
		return webClient;
	}

	@Override
	public Mono<Health> health() {
		return getHealth(recommendationServiceUrl);
	}
}
