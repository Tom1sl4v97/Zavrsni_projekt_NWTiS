package org.foi.nwtis.ttomiek.aplikacija_3.podaci;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.foi.nwtis.podaci.Aerodrom;
import org.foi.nwtis.rest.podaci.Lokacija;

/**
 * Klasa za upravljanje bazom podataka za aerodrome koji se prate.
 */
public class AerodromiPraceniDAO {

	/**
	 * Metoda dodaje novi aerodroma za pratiti u bazu podataka.
	 *
	 * @param icao parametar icao koda aerodroma
	 * @param veza parametar veze
	 * @return vraća true ako je uspiješno zapisao aerodrom, inače false
	 */
	public boolean dodajNovogPracenogAerodroma(String icao, Connection veza) {
		if (provjeriNovogAerodroma(icao, veza)) {
			return false;
		}

		String upit = "INSERT INTO AERODROMI_PRACENI (IDENT, `STORED`) VALUES (?, ?)";

		DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		LocalDateTime datumVrijeme = LocalDateTime.now();

		try (PreparedStatement s = veza.prepareStatement(upit)) {

			s.setString(1, icao);
			s.setString(2, format.format(datumVrijeme));

			int brojAzuriranja = s.executeUpdate();

			return brojAzuriranja == 1;

		} catch (Exception ex) {
			Logger.getLogger(AerodromiPraceniDAO.class.getName()).log(Level.SEVERE, null, ex);
		}
		return false;
	}

	/**
	 * Metoda provjerava da li postoji već korisnikov aerodrom u bazi podataka.
	 *
	 * @param icao parametar icao koda aerodroma
	 * @param veza parametar veze
	 * @return vraća true ako je aerodrom već zapisani u bazi podataka, inače false
	 */
	private boolean provjeriNovogAerodroma(String icao, Connection veza) {
		String upit = "SELECT IDENT FROM AERODROMI_PRACENI WHERE IDENT = \"" + icao + "\"";

		try (Statement s = veza.createStatement(); ResultSet rezultat = s.executeQuery(upit)) {

			while (rezultat.next()) {
				if (rezultat.getString("IDENT").equals(icao)) {
					return true;
				}
			}
		} catch (Exception ex) {
			System.out.println("POGRESAN NAZIV ICAO AERODROMA: " + icao);
			Logger.getLogger(AerodromiPraceniDAO.class.getName()).log(Level.SEVERE, null, ex);
		}
		return false;
	}

	/**
	 * Metoda dohvaća sve prateće aerodrome iz baze podataka.
	 *
	 * @param veza parametar veze
	 * @return vraća listu svih pratećih aerodroma
	 */
	public List<Aerodrom> dohvatiSvePrateceAerodrome(Connection veza) {
		String upit = "SELECT a.* FROM airports a INNER JOIN AERODROMI_PRACENI ap ON ap.ident = a.ident";

		try (Statement s = veza.createStatement(); ResultSet rezultat = s.executeQuery(upit)) {

			List<Aerodrom> listaPracenihAerodroma = new ArrayList<Aerodrom>();

			while (rezultat.next()) {
				String icao = rezultat.getString("IDENT");
				String naziv = rezultat.getString("NAME");
				String drzava = rezultat.getString("CONTINENT");
				String geoLokacija = rezultat.getString("COORDINATES");
				String[] koordinate;

				try {
					koordinate = geoLokacija.split(", ");
				} catch (Exception e) {
					return null;
				}

				Aerodrom podaciAerodroma = new Aerodrom(icao, naziv, drzava,
						new Lokacija(koordinate[0], koordinate[1]));
				listaPracenihAerodroma.add(podaciAerodroma);
			}
			return listaPracenihAerodroma;

		} catch (Exception ex) {
			Logger.getLogger(AerodromiPraceniDAO.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}
}
