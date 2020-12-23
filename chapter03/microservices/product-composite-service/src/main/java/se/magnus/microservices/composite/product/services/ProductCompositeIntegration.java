package se.magnus.microservices.composite.product.services;

import static reactor.core.publisher.Flux.empty;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import se.magnus.api.core.product.Product;
import se.magnus.api.core.product.ProductService;
import se.magnus.api.core.recommendation.Recommendation;
import se.magnus.api.core.recommendation.RecommendationService;
import se.magnus.api.core.review.Review;
import se.magnus.api.core.review.ReviewService;
import se.magnus.api.event.Event;
import se.magnus.util.exceptions.InvalidInputException;
import se.magnus.util.exceptions.NotFoundException;
import se.magnus.util.http.HttpErrorInfo;

@EnableBinding(ProductCompositeIntegration.MessageSources.class)
@Component
public class ProductCompositeIntegration implements ProductService, RecommendationService, ReviewService {

	private static final Logger LOG = LoggerFactory.getLogger(ProductCompositeIntegration.class);

	private final String productServiceUrl = "http://product";
	private final String recommendationServiceUrl = "http://recommendation";
	private final String reviewServiceUrl = "http://review";

	private final ObjectMapper mapper;
	private final WebClient.Builder webClientBuilder;

	private WebClient webClient;
	
	private MessageSources messageSources;
	
	public interface MessageSources {
		
		String OUTPUT_PRODUCTS = "output-products";
		String OUTPUT_RECOMMENDATIONS = "output-recommendations";
		String OUTPUT_REVIEWS = "output-reviews";
		
		@Output(OUTPUT_PRODUCTS)
		MessageChannel outputProducts();
		
		@Output(OUTPUT_RECOMMENDATIONS)
		MessageChannel outputRecommendations();
		
		@Output(OUTPUT_REVIEWS)
		MessageChannel outputReviews();
	}
	
	@Autowired
	public ProductCompositeIntegration(
		WebClient.Builder webClientBuilder,
		ObjectMapper mapper,
		MessageSources messageSources
	) {
		this.webClientBuilder = webClientBuilder;
		this.mapper = mapper;
		this.messageSources = messageSources;
	}
	
	@Override
	public Flux<Review> getReviews(int productId) {

		String url = reviewServiceUrl + "/review?productId=" + productId;
		LOG.debug("Will call the getReviews API on URL: {}", url);

		return getWebClient().get().uri(url).retrieve().bodyToFlux(Review.class).onErrorResume(error -> empty());
			
	}

	@Override
	public Review createReview(Review body) {
		messageSources.outputReviews().send(MessageBuilder.withPayload(
				new Event<Integer, Review>(Event.Type.CREATE, body.getProductId(), body)).build());
		
		return body;
	}

	@Override
	public void deleteReviews(int productId) {
		messageSources.outputReviews().send(MessageBuilder.withPayload(
				new Event<Integer, Review>(Event.Type.DELETE, productId, null)).build());
	}

	@Override
	public Flux<Recommendation> getRecommendations(int productId) {

		String url = recommendationServiceUrl + "/recommendation?productId=" + productId;
		LOG.debug("Will call the getRecommendations API on URL: {}", url);
		
		return getWebClient().get().uri(url).retrieve().bodyToFlux(Recommendation.class)
				.log().onErrorResume(error -> empty());			
	}

	@Override
	public Recommendation createRecommendation(Recommendation body) {
		messageSources.outputRecommendations().send(MessageBuilder.withPayload(
				new Event<Integer, Recommendation>(Event.Type.CREATE, body.getProductId(), body)).build());
		return body;
	}

	@Override
	public void deleteRecommendations(int productId) {
		messageSources.outputRecommendations().send(MessageBuilder.withPayload(
				new Event<Integer, Recommendation>(Event.Type.DELETE, productId, null)).build());
	}

	@Override
	public Product createProduct(Product body) {
		messageSources.outputProducts().send(MessageBuilder.withPayload(
				new Event<Integer, Product>(Event.Type.CREATE, body.getProductId(), body)).build());
		return body;
	}

	@Override
	public Mono<Product> getProduct(int productId) {

		String url = productServiceUrl + "/product/" + productId;
		LOG.debug("Will call the getProduct API on URL: {}", url);

		return getWebClient().get().uri(url).retrieve().bodyToMono(Product.class)
				.log().onErrorMap(WebClientResponseException.class, ex -> handleHttpClientException(ex));
	}
	
	@Override
	public void deleteProduct(int productId) {
		messageSources.outputProducts().send(MessageBuilder.withPayload(
				new Event<Integer, Product>(Event.Type.DELETE, productId, null)).build());
	}
	
	private WebClient getWebClient() {
		if (webClient == null) {
			webClient = webClientBuilder.build();
		}
		
		return webClient;
	}
	
	private Throwable handleHttpClientException(Throwable ex) {
		
		if (!(ex instanceof WebClientResponseException)) {
			LOG.warn("Got a unexpected error: {}, will rethrow it", ex.toString());
			return ex;
		}
		
		WebClientResponseException wcre = (WebClientResponseException)ex;
		
		switch (wcre.getStatusCode()) {
			case NOT_FOUND:
				return new NotFoundException(getErrorMessage(wcre));
			case UNPROCESSABLE_ENTITY:
				return new InvalidInputException(getErrorMessage(wcre));
			default:
				LOG.warn("Got a unexpected HTTP error: {}, will rethrow it", wcre.getStatusCode());
				LOG.warn("Error body: {}", wcre.getResponseBodyAsString());
				return ex;
		}
	}
	
    private String getErrorMessage(WebClientResponseException ex) {
        try {
            return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
        } catch (IOException ioex) {
            return ex.getMessage();
        }
    }

}
