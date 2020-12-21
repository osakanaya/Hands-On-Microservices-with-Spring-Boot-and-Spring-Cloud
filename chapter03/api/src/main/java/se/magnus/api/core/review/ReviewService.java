package se.magnus.api.core.review;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import reactor.core.publisher.Flux;

public interface ReviewService {

	@GetMapping(
		value = "/review",
		produces = "application/json"
	)
	Flux<Review> getReviews(@RequestParam(value = "productId", required = true) int productId);
	
	Review createReview(@RequestBody Review body);
	
	void deleteReviews(@RequestParam(value = "productId", required = true) int productId);
}
