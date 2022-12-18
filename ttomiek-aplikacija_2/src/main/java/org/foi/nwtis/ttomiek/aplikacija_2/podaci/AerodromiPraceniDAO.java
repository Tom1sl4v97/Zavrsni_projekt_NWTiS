package org.foi.nwtis.ttomiek.aplikacija_2.podaci;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
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
	 * Metoda dohvaća sve pracene aerodrome.
	 *
	 * @param veza parametar veze
	 * @return vraća listu svih pratećih aerodroma
	 */
	public List<Aerodrom> dohvatiSvePraceneAerodrome(Connection veza) {
		String upit = "SELECT a.* FROM AERODROMI_PRACENI ap INNER JOIN airports a ON ap.ident = a.ident";

		List<Aerodrom> listaPracenihAerodroma = new ArrayList<>();

		try (Statement s = veza.createStatement(); ResultSet rezultat = s.executeQuery(upit)) {

			while (rezultat.next()) {
				String ident = rezultat.getString("IDENT");
				String name = rezultat.getString("NAME");
				String continent = rezultat.getString("CONTINENT");
				String coordinates = rezultat.getString("COORDINATES");
				String[] koordinate;

				try {
					koordinate = coordinates.split(", ");
				} catch (Exception e) {
					zapisiNoviProblem(ident, "Problem kod čitanja koordinata aerodroma - krivi zapis koordinata", veza);
					return null;
				}
				Aerodrom ad = new Aerodrom(ident, name, continent, new Lokacija(koordinate[0], koordinate[1]));
				listaPracenihAerodroma.add(ad);
			}
			return listaPracenihAerodroma;

		} catch (SQLException ex) {
			Logger.getLogger(AerodromiPraceniDAO.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	/**
	 * Metoda zapisuje novi problem, ako se je dogodio.
	 *
	 * @param icao    string vrijednost icao koda aerodroma
	 * @param problem string vrijednost opisa problema
	 * @param veza    parametar veze
	 */
	private void zapisiNoviProblem(String icao, String problem, Connection veza) {
		DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		LocalDateTime datumVrijeme = LocalDateTime.now();

		AerodromiProblemi ap = new AerodromiProblemi(icao, problem, format.format(datumVrijeme));
		AerodromiProblemiDAO apdao = new AerodromiProblemiDAO();
		apdao.dodajNoviProblem(ap, veza);
	}
}
