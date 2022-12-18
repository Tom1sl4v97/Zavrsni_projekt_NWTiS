package org.foi.nwtis.ttomiek.aplikacija_3.podaci;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.foi.nwtis.podaci.Aerodrom;
import org.foi.nwtis.rest.podaci.Lokacija;

/**
 * Klasa za upravljanje bazom podataka za aerodrome.
 */
public class AerodromiDAO {

	/**
	 * Metoda kojom se dohvaćaju podaci aerodroma iz baze podataka.
	 *
	 * @param icao parametar icao koda aerodroma
	 * @param veza parametar veze
	 * @return vraća traženi aerodrom, inače null
	 */
	public Aerodrom dohvatiAerodrom(String icao, Connection veza) {
		String upit = "SELECT * FROM airports WHERE IDENT = ?";

		try (PreparedStatement ps = veza.prepareStatement(upit)) {

			ps.setString(1, icao);
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				String ident = rs.getString("IDENT");
				String name = rs.getString("NAME");
				String continent = rs.getString("CONTINENT");
				String coordinates = rs.getString("COORDINATES");
				String[] koordinate;

				try {
					koordinate = coordinates.split(", ");
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}

				Aerodrom ad = new Aerodrom(ident, name, continent, new Lokacija(koordinate[0], koordinate[1]));
				return ad;
			}

		} catch (SQLException ex) {
			Logger.getLogger(AerodromiDAO.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	/**
	 * Metoda dohvaća sve aerodrome iz baze podataka.
	 *
	 * @param veza parametar veze
	 * @return vraća listu aerodroma, inače null
	 */
	public List<Aerodrom> dohvatiSveAerodrome(Connection veza) {
		String upit = "SELECT * FROM airports";

		List<Aerodrom> listaAerodroma = new ArrayList<>();
		try (Statement s = veza.createStatement(); ResultSet rs = s.executeQuery(upit)) {
			while (rs.next()) {
				String ident = rs.getString("IDENT");
				String name = rs.getString("NAME");
				String continent = rs.getString("CONTINENT");
				String coordinates = rs.getString("COORDINATES");
				String[] koordinate;

				try {
					koordinate = coordinates.split(", ");
				} catch (Exception e) {
					return null;
				}
				Aerodrom ad = new Aerodrom(ident, name, continent, new Lokacija(koordinate[0], koordinate[1]));
				listaAerodroma.add(ad);
			}
			return listaAerodroma;
		} catch (SQLException ex) {
			Logger.getLogger(AerodromiDAO.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}
}
