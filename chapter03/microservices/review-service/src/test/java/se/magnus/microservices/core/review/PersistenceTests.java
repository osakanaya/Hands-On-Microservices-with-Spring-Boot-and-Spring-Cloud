package se.magnus.microservices.core.review;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import se.magnus.microservices.core.review.persistence.ReviewEntity;
import se.magnus.microservices.core.review.persistence.ReviewRepository;

@DataJpaTest(properties = {"spring.cloud.config.enabled=false"})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class PersistenceTests {

	@Autowired
	private ReviewRepository repository;
	
	private ReviewEntity savedEntity;
	
	@BeforeEach
	public void setUpDb() {
		repository.deleteAll();

		ReviewEntity entity = new ReviewEntity(1, 2, "a", "s", "c");
		savedEntity = repository.save(entity);

		assertEqualsReview(entity, savedEntity);
	}

	@Test
	public void create() {
		ReviewEntity newEntity = new ReviewEntity(1, 3, "a", "s", "c");
		repository.save(newEntity);

		ReviewEntity foundEntity = repository.findById(newEntity.getId()).get();
		assertEqualsReview(newEntity, foundEntity);

		assertEquals(2, repository.count());
	}

	@Test
	public void update() {
		savedEntity.setAuthor("a2");
		repository.save(savedEntity);

		ReviewEntity foundEntity = repository.findById(savedEntity.getId()).get();
		assertEquals(1, (long) foundEntity.getVersion());
		assertEquals("a2", foundEntity.getAuthor());
	}

	@Test
	public void delete() {
		repository.delete(savedEntity);
		assertFalse(repository.existsById(savedEntity.getId()));
	}

	@Test
	public void getByProductId() {
		List<ReviewEntity> entityList = repository.findByProductId(savedEntity.getProductId());

		assertThat(entityList, hasSize(1));
		assertEqualsReview(savedEntity, entityList.get(0));
	}

	@Test
	public void duplicateError() {
		assertThrows(DataIntegrityViolationException.class, () -> {
			ReviewEntity entity = new ReviewEntity(1, 2, "a", "s", "c");
			repository.save(entity);
		});
	}

	@Test
	public void optimisticLockError() {
		ReviewEntity entity1 = repository.findById(savedEntity.getId()).get();
		ReviewEntity entity2 = repository.findById(savedEntity.getId()).get();

		entity1.setAuthor("a1");
		repository.save(entity1);

		try {
			entity2.setAuthor("a2");
			repository.save(entity2);

			fail("Expected an OptimisticLockingFailureException");
		} catch (OptimisticLockingFailureException ex) {
		}

		ReviewEntity updatedEntity = repository.findById(savedEntity.getId()).get();
		assertEquals(1, (long) updatedEntity.getVersion());
		assertEquals("a1", updatedEntity.getAuthor());
	}

	private void assertEqualsReview(ReviewEntity expectedEntity, ReviewEntity actualEntity) {
		assertEquals(expectedEntity.getId(), actualEntity.getId());
		assertEquals(expectedEntity.getVersion(), actualEntity.getVersion());
		assertEquals(expectedEntity.getProductId(), actualEntity.getProductId());
		assertEquals(expectedEntity.getReviewId(), actualEntity.getReviewId());
		assertEquals(expectedEntity.getAuthor(), actualEntity.getAuthor());
		assertEquals(expectedEntity.getSubject(), actualEntity.getSubject());
		assertEquals(expectedEntity.getContent(), actualEntity.getContent());
	}
	
}
