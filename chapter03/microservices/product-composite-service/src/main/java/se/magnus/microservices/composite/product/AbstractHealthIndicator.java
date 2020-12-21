package se.magnus.microservices.composite.product;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

public abstract class AbstractHealthIndicator implements ReactiveHealthIndicator {
	
	private static final Logger LOG = LoggerFactory.getLogger(AbstractHealthIndicator.class);

	protected Mono<Health> getHealth(String url) {
		url += "/actuator/health";
		
		LOG.debug("Will call the Health API on URL: {}", url);
		
		return getWebClient().get().uri(url).retrieve().bodyToMono(String.class)
				.map(s -> new Health.Builder().up().build())
				.onErrorResume(ex -> Mono.just(new Health.Builder().down(ex).build()))
				.log();
	}
	
	protected abstract WebClient getWebClient();

	@Override
	public abstract Mono<Health> health();
}
