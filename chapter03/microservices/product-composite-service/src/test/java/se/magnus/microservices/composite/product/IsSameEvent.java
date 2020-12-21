package se.magnus.microservices.composite.product;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import se.magnus.api.event.Event;

public class IsSameEvent extends TypeSafeMatcher<String> {

	private static final Logger LOG = LoggerFactory.getLogger(IsSameEvent.class);
	
	private ObjectMapper mapper = new ObjectMapper();
	
	@SuppressWarnings("rawtypes")
	private Event expectedEvent;
	
	@SuppressWarnings("rawtypes")
	private IsSameEvent(Event expectedEvent) {
		this.expectedEvent = expectedEvent;
	}

	@Override
	public void describeTo(Description description) {
		String expectedJson = convertObjectToJsonString(expectedEvent);
		description.appendText("expected to looklike " + expectedJson);
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected boolean matchesSafely(String eventAsJson) {
		
		if (expectedEvent == null) return false;
		
		LOG.trace("Convert the following json string to a map: {}", eventAsJson);
		Map mapEvent = convertJsonStringToMap(eventAsJson);
		mapEvent.remove("eventCreatedAt");
		
		Map mapExpectedEvent = getMapWithoutCreatedAt(expectedEvent);
		
		LOG.trace("Got the map: {}",mapEvent);
		LOG.trace("Compare to the expected map: {}", mapExpectedEvent);
		
		return mapEvent.equals(mapExpectedEvent);
	}
	
	@SuppressWarnings("rawtypes")
	private Map getMapWithoutCreatedAt(Event event) {
		Map mapEvent = convertObjectToMap(event);
		mapEvent.remove("eventCreatedAt");
		return mapEvent;
	}
	
	@SuppressWarnings("rawtypes")
	private Map convertObjectToMap(Object object) {
		JsonNode node = mapper.convertValue(object, JsonNode.class);
		return mapper.convertValue(node, Map.class);
	}
	
	@SuppressWarnings("rawtypes")
	public static Matcher<String> sameEventExceptCreatedAt(Event expectedEvent) {
		return new IsSameEvent(expectedEvent);
	}
	
	private String convertObjectToJsonString(Object object) {
		try {
			return mapper.writeValueAsString(object);
		} catch (JsonProcessingException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	@SuppressWarnings("rawtypes")
	private Map convertJsonStringToMap(String eventAsJson) {
		try {
			return mapper.readValue(eventAsJson, new TypeReference<HashMap>(){});
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}
	
}
