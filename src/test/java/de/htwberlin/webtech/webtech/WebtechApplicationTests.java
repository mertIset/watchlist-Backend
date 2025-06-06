package de.htwberlin.webtech.webtech;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test") // Verwendet application-test.properties
class WebtechApplicationTests {

	@Test
	void contextLoads() {
		// Test überprüft, ob der Spring-Kontext erfolgreich geladen wird
	}

}