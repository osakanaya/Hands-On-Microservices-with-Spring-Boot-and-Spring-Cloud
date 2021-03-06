package se.magnus.microservices.core.product.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;
import se.magnus.api.core.product.Product;
import se.magnus.api.core.product.ProductService;
import se.magnus.microservices.core.product.persistence.ProductEntity;
import se.magnus.microservices.core.product.persistence.ProductRepository;
import se.magnus.util.exceptions.InvalidInputException;
import se.magnus.util.exceptions.NotFoundException;
import se.magnus.util.http.ServiceUtil;

import static reactor.core.publisher.Mono.error;

import java.util.Random;

@RestController
public class ProductServiceImpl implements ProductService {

	private static final Logger LOG = LoggerFactory.getLogger(ProductServiceImpl.class);
	
	private final ServiceUtil serviceUtil;
	private final ProductRepository repository;
	private final ProductMapper mapper;
	
	@Autowired
	public ProductServiceImpl(ProductRepository repository, ProductMapper mapper, ServiceUtil serviceUtil) {
		this.repository = repository;
		this.mapper = mapper;
		this.serviceUtil = serviceUtil;
	}
	
	@Override
	public Mono<Product> getProduct(HttpHeaders headers, int productId, int delay, int faultPercent) {
		LOG.debug("/product return the found product for product={}", productId);

		if (productId < 1) throw new InvalidInputException("Invalid productId: " + productId);
		
		if (delay > 0) simulateDelay(delay);
		
		if (faultPercent > 0) throwErrorIfBadLuck(faultPercent);

        LOG.info("Will get product info for id={}", productId);

		return repository.findByProductId(productId)
			.switchIfEmpty(error(new NotFoundException("No product found for productId: " + productId)))
			.log()
			.map(e -> mapper.entityToApi(e))
			.map(e -> {
				e.setServiceAddress(serviceUtil.getServiceAddress());
				return e;
			});
	}

	@Override
	public Product createProduct(Product body) {
		if (body.getProductId() < 1) throw new InvalidInputException("Invalid productId: " + body.getProductId());
		
		ProductEntity entity = mapper.apiToEntity(body);
		Mono<Product> newEntity = repository.save(entity)
			.log()
			.onErrorMap(DuplicateKeyException.class, ex -> new InvalidInputException("Duplicate Key, Product Id: " + body.getProductId()))
			.map(e -> mapper.entityToApi(e));
		
		return newEntity.block();
	}

	@Override
	public void deleteProduct(int productId) {
        if (productId < 1) throw new InvalidInputException("Invalid productId: " + productId);

        repository.findByProductId(productId)
			.log()
			.map(e -> repository.delete(e)).flatMap(e -> e).block();
	}
	
	private void simulateDelay(int delay) {
		LOG.debug("Sleeping for {} seconds...", delay);
		
		try {
			Thread.sleep(delay * 1000);
		} catch (InterruptedException ex) {
		}
		
		LOG.debug("Moving on...");
	}
	
	private void throwErrorIfBadLuck(int faultPercent) {
		int randomThreshold = getRandomNumber(1, 100);
		
		if (faultPercent < randomThreshold) {
			LOG.debug("We got lucky, no error occurred, {} < {}", faultPercent, randomThreshold);
		} else {
			LOG.debug("Bad luck, an error occurred, {} >= {}", faultPercent, randomThreshold);
			throw new RuntimeException("Something went wrong...");
		}
	}
	
	private final Random randomNumberGenerator = new Random();
	
	private int getRandomNumber(int min ,int max) {
		if (max < min) {
			throw new RuntimeException("Max must be greater than min");
		}
		
		return randomNumberGenerator.nextInt((max - min) + 1) + min;
	}
}
