package se.magnus.microservices.composite.product.services;

import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import reactor.core.publisher.Mono;
import se.magnus.api.composite.product.ProductAggregate;
import se.magnus.api.composite.product.ProductCompositeService;
import se.magnus.api.composite.product.RecommendationSummary;
import se.magnus.api.composite.product.ReviewSummary;
import se.magnus.api.composite.product.ServiceAddresses;
import se.magnus.api.core.product.Product;
import se.magnus.api.core.recommendation.Recommendation;
import se.magnus.api.core.review.Review;
import se.magnus.util.exceptions.NotFoundException;
import se.magnus.util.http.ServiceUtil;

@RestController
public class ProductCompositeServiceImpl implements ProductCompositeService {

    private static final Logger LOG = LoggerFactory.getLogger(ProductCompositeServiceImpl.class);

    private final SecurityContext nullSC = new SecurityContextImpl();

    private final ServiceUtil serviceUtil;
	private ProductCompositeIntegration integration;

	@Autowired
	public ProductCompositeServiceImpl(ServiceUtil serviceUtil, ProductCompositeIntegration integration) {
		this.serviceUtil = serviceUtil;
		this.integration = integration;
	}
	
	@Override
	public Mono<Void> createCompositeProduct(ProductAggregate body) {
		return ReactiveSecurityContextHolder.getContext().doOnSuccess(sc -> internalCreateCompositeProduct(sc, body)).then();
	}
	
	private void internalCreateCompositeProduct(SecurityContext sc, ProductAggregate body) {
		try {
			
			logAuthorizationinfo(sc);

            LOG.debug("createCompositeProduct: creates a new composite entity for productId: {}", body.getProductId());

            Product product = new Product(body.getProductId(), body.getName(), body.getWeight(), null);
            integration.createProduct(product);
            
            if (body.getRecommendations() != null) {
            	body.getRecommendations().forEach(r -> {
            		Recommendation recommendation = new Recommendation(body.getProductId(), r.getRecommendationId(), r.getAuthor(), 
        				r.getRate(), r.getContent(), null);
            		integration.createRecommendation(recommendation);
            	});
            }
            
            if (body.getReviews() != null) {
            	body.getReviews().forEach(r -> {
            		Review review = new Review(body.getProductId(), r.getReviewId(), r.getAuthor(), r.getSubject(), r.getContent(), null);
            		integration.createReview(review);
            	});
            }
            
            LOG.debug("createCompositeProduct: composite entites created for productId: {}", body.getProductId());

		} catch (RuntimeException ex) {
			LOG.warn("createCompositeProduct failed", ex);
			throw ex;
		}
	}

	@Override
	public Mono<Void> deleteCompositeProduct(int productId) {
		return ReactiveSecurityContextHolder.getContext().doOnSuccess(sc -> internalDeleteCompositeProduct(sc, productId)).then();
	}
	
	private void internalDeleteCompositeProduct(SecurityContext sc, int productId) {
		try {
			logAuthorizationinfo(sc);
			
	        LOG.debug("deleteCompositeProduct: Deletes a product aggregate for productId: {}", productId);

	        integration.deleteProduct(productId);
	        integration.deleteRecommendations(productId);
	        integration.deleteReviews(productId);
	        
	        LOG.debug("deleteCompositeProduct: aggregate entities deleted for productId: {}", productId);
			
		} catch (RuntimeException ex) {
            LOG.warn("deleteCompositeProduct failed: {}", ex.toString());
            throw ex;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Mono<ProductAggregate> getCompositeProduct(HttpHeaders requestHeaders, int productId, int delay, int faultPercent) {
		
        LOG.info("Will get composite product info for product.id={}", productId);

		HttpHeaders headers = getHeaders(requestHeaders, "X-group");
		
		return Mono.zip(
			values -> createProductAggregate((SecurityContext)values[0],
					(Product)values[1], (List<Recommendation>)values[2], 
					(List<Review>)values[3], serviceUtil.getServiceAddress()),
			ReactiveSecurityContextHolder.getContext().defaultIfEmpty(nullSC),
			integration.getProduct(headers, productId, delay, faultPercent)
				.onErrorReturn(CallNotPermittedException.class, getProductFallbackValue(productId)),
			integration.getRecommendations(headers, productId).collectList(),
			integration.getReviews(headers, productId).collectList()
		)
		.doOnError(ex -> LOG.warn("getCompositeProduct failed: {}", ex.toString()))
		.log();
	}
	
	private HttpHeaders getHeaders(HttpHeaders requestHeaders, String... headers) {
		LOG.trace("Will look for {} headers: {}", headers.length, headers);
		
		HttpHeaders h = new HttpHeaders();		
		for (String header : headers) {
			List<String> value = requestHeaders.get(header);
			if (value != null) {
				h.addAll(header, value);
			}
		}
		
		LOG.trace("Will transfer {}, headers: {}", h.size(), h);
		
		return h;
	}
	
	private Product getProductFallbackValue(int productId) {
		if (productId == 13) {
			throw new NotFoundException("Product Id: " + productId + " not found in fallback cache!");
		}
		
		return new Product(productId, "Fallback product" + productId, productId, serviceUtil.getServiceAddress());
	}

    private ProductAggregate createProductAggregate(SecurityContext sc, Product product, List<Recommendation> recommendations, List<Review> reviews, String serviceAddress) {

    	logAuthorizationinfo(sc);
    	
        int productId = product.getProductId();
        String name = product.getName();
        int weight = product.getWeight();

        List<RecommendationSummary> recommendationSummaries = (recommendations == null) ? null :
             recommendations.stream()
                .map(r -> new RecommendationSummary(r.getRecommendationId(), r.getAuthor(), r.getRate(), r.getContent()))
                .collect(Collectors.toList());

        List<ReviewSummary> reviewSummaries = (reviews == null)  ? null :
            reviews.stream()
                .map(r -> new ReviewSummary(r.getReviewId(), r.getAuthor(), r.getSubject(), r.getContent()))
                .collect(Collectors.toList());

        String productAddress = product.getServiceAddress();
        String reviewAddress = (reviews != null && reviews.size() > 0) ? reviews.get(0).getServiceAddress() : "";
        String recommendationAddress = (recommendations != null && recommendations.size() > 0) ? recommendations.get(0).getServiceAddress() : "";
        ServiceAddresses serviceAddresses = new ServiceAddresses(serviceAddress, productAddress, reviewAddress, recommendationAddress);

        return new ProductAggregate(productId, name, weight, recommendationSummaries, reviewSummaries, serviceAddresses);
    }
    
    private void logAuthorizationinfo(SecurityContext sc) {
    	if (sc != null && sc.getAuthentication() != null && sc.getAuthentication() instanceof JwtAuthenticationToken) {
    		Jwt jwtToken = ((JwtAuthenticationToken)sc.getAuthentication()).getToken();
    		logAuthorizationInfo(jwtToken);
    	} else {
    		LOG.warn("NO JWT based Authentication supplied, running tests are we?");
    	}
    }
    
    private void logAuthorizationInfo(Jwt jwt) {
    	if (jwt == null) {
    		LOG.warn("NO JWT supplied, running tests are we?");
    	} else {
    		if (LOG.isDebugEnabled()) {
    			URL issuer = jwt.getIssuer();
    			List<String> audience = jwt.getAudience();
    			Object subject = jwt.getClaims().get("sub");
    			Object scopes = jwt.getClaims().get("scope");
    			Object expires = jwt.getClaims().get("exp");
    			
    			LOG.debug("Authorization info: Subject:{}, scopes:{}, expires:{}, issuer:{}, audience:{}",
    					subject, scopes, expires, issuer, audience);
    		}
    	}
    }

}
