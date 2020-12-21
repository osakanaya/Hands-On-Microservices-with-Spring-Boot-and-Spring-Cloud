package se.magnus.microservices.composite.product;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@Component
public class ProductHealthIndicator extends AbstractHealthIndicator {

	private final WebClient webClient;
	
	private final String productServiceUrl;

	@Autowired
	public ProductHealthIndicator(
		WebClient.Builder webClient,
		@Value("${app.product-service.host}")
		String productServiceHost,
		@Value("${app.product-service.port}")
		int productServicePort
	) {
		this.webClient = webClient.build();
		this.productServiceUrl = "http://" + productServiceHost + ":" + productServicePort;
	}
	
	@Override
	protected WebClient getWebClient() {
		return webClient;
	}

	@Override
	public Mono<Health> health() {
		return getHealth(productServiceUrl);
	}
}
