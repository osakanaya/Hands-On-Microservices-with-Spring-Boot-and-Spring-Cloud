package se.magnus.microservices.core.product;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

import se.magnus.api.core.product.Product;
import se.magnus.microservices.core.product.persistence.ProductRepository;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import static reactor.core.publisher.Mono.just;
import static org.springframework.http.HttpStatus.*;

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {"spring.data.mongodb.port:0"})
class ProductServiceApplicationTests {

	@Autowired
	private WebTestClient client;
	
	@Autowired
	private ProductRepository repository;
	
	@BeforeEach
	public void setupDb() {
		repository.deleteAll();
	}
	
	@Test
	public void getProductById() {
		
		int productId = 1;
		
		postAndVerifyProduct(productId, OK);
		assertTrue(repository.findByProductId(productId).isPresent());
		
		getAndVeirfyProduct(productId, OK)
			.jsonPath("$.productId").isEqualTo(productId);
	}
	
	@Test
	public void duplicateError() {
		
		int productId = 1;
		
		postAndVerifyProduct(productId, OK);
		assertTrue(repository.findByProductId(productId).isPresent());
		
		postAndVerifyProduct(productId, UNPROCESSABLE_ENTITY)
			.jsonPath("$.path").isEqualTo("/product")
			.jsonPath("$.message").isEqualTo("Duplicate Key, Product Id: " + productId);
		
	}
	
	@Test
	public void deleteProduct() {

		int productId = 1;
		
		postAndVerifyProduct(productId, OK);
		assertTrue(repository.findByProductId(productId).isPresent());

		deleteAndVerifyProduct(productId, OK);
		assertFalse(repository.findByProductId(productId).isPresent());
		
		deleteAndVerifyProduct(productId, OK);
	}
	
	@Test
	public void getProductInvalidParameterString() {
		
		getAndVeirfyProduct("/no-integer", BAD_REQUEST)
			.jsonPath("$.path").isEqualTo("/product/no-integer")
	        .jsonPath("$.message").isEqualTo("Type mismatch.");
	}
	
	@Test
	public void getProductNotFound() {
		
		int productIdNotFound = 13;

		getAndVeirfyProduct(productIdNotFound, NOT_FOUND)
			.jsonPath("$.path").isEqualTo("/product/" + productIdNotFound)
			.jsonPath("$.message").isEqualTo("No product found for productId: " + productIdNotFound);
	}

	@Test
	public void getProductInvalidParamterNegativeValue() {
		
		int productIdInvalid = -1;
		
		getAndVeirfyProduct(productIdInvalid, UNPROCESSABLE_ENTITY)
			.jsonPath("$.path").isEqualTo("/product/" + productIdInvalid)
			.jsonPath("$.message").isEqualTo("Invalid productId: " + productIdInvalid);
	}
	
	private WebTestClient.BodyContentSpec getAndVeirfyProduct(int productId, HttpStatus expectedStatus) {
		return getAndVeirfyProduct("/" + productId, expectedStatus);
	}
	
	private WebTestClient.BodyContentSpec getAndVeirfyProduct(String productIdPath, HttpStatus expectedStatus) {
		return client.get()
			.uri("/product" + productIdPath)
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(expectedStatus)
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody();
	}
	
	private WebTestClient.BodyContentSpec postAndVerifyProduct(int productId, HttpStatus expectedStatus) {
		Product product = new Product(productId, "Name " + productId, productId, "SA");
		
		return client.post()
			.uri("/product")
			.body(just(product), Product.class)
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(expectedStatus)
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody();
	}
	
	private WebTestClient.BodyContentSpec deleteAndVerifyProduct(int productId, HttpStatus expecHttpStatus) {
		return client.delete()
			.uri("/product/" + productId)
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(expecHttpStatus)
			.expectBody();
	}

}
