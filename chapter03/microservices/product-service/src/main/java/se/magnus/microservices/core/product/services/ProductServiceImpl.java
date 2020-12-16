package se.magnus.microservices.core.product.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;

import se.magnus.api.core.product.Product;
import se.magnus.api.core.product.ProductService;
import se.magnus.microservices.core.product.persistence.ProductEntity;
import se.magnus.microservices.core.product.persistence.ProductRepository;
import se.magnus.util.exceptions.InvalidInputException;
import se.magnus.util.exceptions.NotFoundException;
import se.magnus.util.http.ServiceUtil;


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
	public Product getProduct(int productId) {
		LOG.debug("/product return the found product for product={}", productId);

		if (productId < 1) throw new InvalidInputException("Invalid productId: " + productId);

		ProductEntity entity = repository.findByProductId(productId)
			.orElseThrow(() -> new NotFoundException("No product found for productId: " + productId));
		
		Product response = mapper.entityToApi(entity);
		response.setServiceAddress(serviceUtil.getServiceAddress());
		
		return response;
	}

	@Override
	public Product createProduct(Product body) {
		try {
			ProductEntity entity = mapper.apiToEntity(body);
			ProductEntity newEntity = repository.save(entity);
			
			return mapper.entityToApi(newEntity);
		} catch (DuplicateKeyException ex) {
			throw new InvalidInputException("Duplicate Key, Product Id: " + body.getProductId());
		}
	}

	@Override
	public void deleteProduct(int productId) {
		repository.findByProductId(productId).ifPresent(e -> repository.delete(e));
	}

}
