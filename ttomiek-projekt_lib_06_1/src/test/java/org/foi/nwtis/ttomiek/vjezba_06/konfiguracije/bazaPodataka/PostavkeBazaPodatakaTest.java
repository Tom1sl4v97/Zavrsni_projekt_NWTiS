package org.foi.nwtis.ttomiek.vjezba_06.konfiguracije.bazaPodataka;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Properties;

import org.foi.nwtis.ttomiek.vjezba_03.konfiguracije.NeispravnaKonfiguracija;
import org.foi.nwtis.ttomiek.vjezba_06.konfiguracije.bazaPodataka.PostavkeBazaPodataka;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PostavkeBazaPodatakaTest {

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
	}
	private PostavkeBazaPodataka pbp;
	
	@BeforeEach
	void setUp() throws Exception {
		String nazivDatoteke = "NWTiS.db.config_1.xml";
		pbp = new PostavkeBazaPodataka(nazivDatoteke);
		try {
			pbp.ucitajKonfiguraciju();
		} catch (NeispravnaKonfiguracija e) {
			e.printStackTrace();
			fail("Nije uspijelo učitavnje!");
		}
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void testGetDriverDatabase() {
		String result = pbp.getDriverDatabase();
		assertEquals("org.hsqldb.jdbcDriver",result);
	}

	@Test
	void testGetDriverDatabaseString() {
		assertEquals("com.mysql.jdbc.Driver",pbp.getDriverDatabase("jdbc:mysql://localhost/"));
	}

	@Test
	void testGetDriversDatabase() {
		Properties result = pbp.getDriversDatabase();
		Properties expected = new Properties();
		expected.setProperty("jdbc.mysql", "com.mysql.jdbc.Driver");
		expected.setProperty("jdbc.derby", "org.apache.derby.jdbc.ClientDriver");
		expected.setProperty("jdbc.hsqldb.hsql", "org.hsqldb.jdbcDriver");
		assertEquals(expected, result);
	}

}
