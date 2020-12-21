package se.magnus.microservices.composite.product;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import se.magnus.api.core.product.Product;
import se.magnus.api.event.Event;

import static se.magnus.microservices.composite.product.IsSameEvent.sameEventExceptCreatedAt;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

public class IsSameEventTests {

	ObjectMapper mapper = new ObjectMapper();
	
	@Test
	public void testEventObjectCompare() throws JsonProcessingException {

		Event<Integer, Product> event1 = new Event<>(Event.Type.CREATE, 1, new Product(1, "name", 1, null));
		Event<Integer, Product> event2 = new Event<>(Event.Type.CREATE, 1, new Product(1, "name", 1, null));
		Event<Integer, Product> event3 = new Event<>(Event.Type.DELETE, 1, null);
		Event<Integer, Product> event4 = new Event<>(Event.Type.CREATE, 1, new Product(2, "name", 1, null));
		
		String event1JSon = mapper.writeValueAsString(event1);

		assertThat(event1JSon, is(sameEventExceptCreatedAt(event2)));
		assertThat(event1JSon, not(sameEventExceptCreatedAt(event3)));
		assertThat(event1JSon, not(sameEventExceptCreatedAt(event4)));
	}
}
