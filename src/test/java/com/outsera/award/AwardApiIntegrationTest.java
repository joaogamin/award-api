package com.outsera.award;

import com.outsera.award.dto.AwardIntervalResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AwardApiIntegrationTest {

	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	public void testGetProducerIntervals_ShouldReturnMetrics() {
		ResponseEntity<AwardIntervalResponse> response =
				restTemplate.getForEntity("/api/awards/intervals",
						AwardIntervalResponse.class);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		AwardIntervalResponse body = response.getBody();
		assertNotNull(body);
		assertNotNull(body.getMin());
		assertNotNull(body.getMax());
		assertFalse(body.getMin().isEmpty());
		assertFalse(body.getMax().isEmpty());

		assertEquals("Joel Silver", body.getMin().get(0).getProducer());
		assertEquals(1, body.getMin().get(0).getInterval());
		assertEquals(1990, body.getMin().get(0).getPreviousWin());
		assertEquals(1991, body.getMin().get(0).getFollowingWin());

		assertEquals("Matthew Vaughn", body.getMax().get(0).getProducer());
		assertEquals(13, body.getMax().get(0).getInterval());
		assertEquals(2002, body.getMax().get(0).getPreviousWin());
		assertEquals(2015, body.getMax().get(0).getFollowingWin());
	}
}