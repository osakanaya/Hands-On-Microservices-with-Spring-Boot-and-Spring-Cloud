package se.magnus.springcloud.gateway;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.CompositeReactiveHealthContributor;
import org.springframework.boot.actuate.health.ReactiveHealthContributor;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HealthCheckConfiguration {

	@Autowired
	ProductHealthIndicator productHealthIndicator;
	
	@Autowired
	RecommendationHealthIndicator recommendationHealthIndicator;
	
	@Autowired
	ReviewHealthIndicator reviewHealthIndicator;
	
	@Autowired
	ProductCompositeHealthIndicator productCompositeHealthIndicator;
	
	@Bean
	public ReactiveHealthContributor healthCheckCoreServices() {
		
		Map<String, ReactiveHealthIndicator> contributorMap = new HashMap<>();
		contributorMap.put("product", productHealthIndicator);
		contributorMap.put("recommendation", recommendationHealthIndicator);
		contributorMap.put("review", reviewHealthIndicator);
		contributorMap.put("product-composite", productCompositeHealthIndicator);
		
		return CompositeReactiveHealthContributor.fromMap(contributorMap);
	}

}
