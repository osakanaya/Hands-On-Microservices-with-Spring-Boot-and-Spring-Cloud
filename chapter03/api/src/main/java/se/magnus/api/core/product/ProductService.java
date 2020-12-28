package se.magnus.api.core.product;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import reactor.core.publisher.Mono;

public interface ProductService {

	@GetMapping(
		value = "/product/{productId}",
		produces = "application/json"
	)
	Mono<Product> getProduct(
		@PathVariable int productId,
		@RequestParam(value = "delay", required = false, defaultValue = "0") int delay,
		@RequestParam(value = "faultPercent", required = false, defaultValue = "0") int faultPercent
	);
	
	Product createProduct(Product body);
	
	void deleteProduct(int productId);
}
