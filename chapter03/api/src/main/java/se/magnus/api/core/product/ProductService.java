package se.magnus.api.core.product;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import reactor.core.publisher.Mono;

public interface ProductService {

	@GetMapping(
		value = "/product/{productId}",
		produces = "application/json"
	)
	Mono<Product> getProduct(@PathVariable int productId);
	
	Product createProduct(Product body);
	
	void deleteProduct(int productId);
}
