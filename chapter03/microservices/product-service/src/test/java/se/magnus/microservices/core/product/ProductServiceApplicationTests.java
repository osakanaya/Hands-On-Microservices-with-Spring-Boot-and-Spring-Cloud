package se.magnus.microservices.core.product;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.http.HttpStatus;
import org.springframework.integration.channel.AbstractMessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.web.reactive.server.WebTestClient;

import se.magnus.api.core.product.Product;
import se.magnus.api.event.Event;
import se.magnus.microservices.core.product.persistence.ProductRepository;
import se.magnus.util.exceptions.InvalidInputException;

@SpringBootTest(
	webEnvironment=RANDOM_PORT, 
	properties = {
		"spring.data.mongodb.port: 0", 
		"spring.cloud.config.enabled=false", 
		"server.error.include-message=always"
	}
)
class ProductServiceApplicationTests {

	@Autowired
	private WebTestClient client;
	
	@Autowired
	private ProductRepository repository;
	
	@Autowired
	private Sink channels;

	private AbstractMessageChannel input = null;
	
	@BeforeEach
	public void setupDb() {
		input = (AbstractMessageChannel)channels.input();
		repository.deleteAll().block();
	}
	
	@Test
	public void getProductById() {
		
		int productId = 1;

		assertNull(repository.findByProductId(productId).block());
		assertEquals(0, (long)repository.count().block());
		
		sendCreateProductEvent(productId);
		
		assertNotNull(repository.findByProductId(productId).block());
		assertEquals(1, (long)repository.count().block());
		
		getAndVeirfyProduct(productId, OK)
			.jsonPath("$.productId").isEqualTo(productId);
	}
	
	@Test
	public void duplicateError() {
		
		int productId = 1;
		
		assertNull(repository.findByProductId(productId).block());
		assertEquals(0, (long)repository.count().block());

		sendCreateProductEvent(productId);

		assertNotNull(repository.findByProductId(productId).block());
		assertEquals(1, (long)repository.count().block());

		try {
			sendCreateProductEvent(productId);
			
			fail("Expected a MessagingException here!");
		} catch (MessagingException me) {
			if (me.getCause() instanceof InvalidInputException) {
				InvalidInputException iie = (InvalidInputException)me.getCause();
				assertEquals("Duplicate Key, Product Id: " + productId, iie.getMessage());
			} else {
				fail("Expected a InvalidInputException as the root cause!");
			}
		}
	}
	
	@Test
	public void deleteProduct() {

		int productId = 1;
		
		assertNull(repository.findByProductId(productId).block());
		assertEquals(0, (long)repository.count().block());

		sendCreateProductEvent(productId);

		assertNotNull(repository.findByProductId(productId).block());
		assertEquals(1, (long)repository.count().block());

		sendDeleteProductEvent(productId);

		assertNull(repository.findByProductId(productId).block());
		assertEquals(0, (long)repository.count().block());
		
		sendDeleteProductEvent(productId);
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
	
	private void sendCreateProductEvent(int productId) {
		Product product = new Product(productId, "Name " + productId, productId, "SA");
		Event<Integer, Product> event = new Event<>(Event.Type.CREATE, productId, product);
		input.send(new GenericMessage<>(event));
	}

	private void sendDeleteProductEvent(int productId) {
		Event<Integer, Product> event = new Event<>(Event.Type.DELETE, productId, null);
		input.send(new GenericMessage<>(event));
	}
}
