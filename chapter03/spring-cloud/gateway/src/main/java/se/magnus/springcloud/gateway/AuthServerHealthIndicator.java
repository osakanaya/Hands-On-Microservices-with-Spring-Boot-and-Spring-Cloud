package se.magnus.springcloud.gateway;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@Component
public class AuthServerHealthIndicator extends AbstractHealthIndicator {

	private final WebClient.Builder webClientBuilder;

	private WebClient webClient;
	
	@Autowired
	public AuthServerHealthIndicator(
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
		return getHealth("http://auth-server");
	}
}
