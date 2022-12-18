package org.foi.nwtis.ttomiek.aplikacija_3.podaci;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Klasa za upravljanje bazom podataka za uloge korisnika.
 */
public class UlogeDAO {

	/**
	 * Metoda provjerava ulogu korisnika.
	 *
	 * @param korisnik string vrijednost korisničkog imena
	 * @param grupa    string vrijednost grupe
	 * @param veza     parametar veze
	 * @return vraća true ako je korisnik u grupi, inače false
	 */
	public boolean provjeriUloguKorisnika(String korisnik, String grupa, Connection veza) {
		String upit = "SELECT * FROM ULOGE WHERE KORISNIK = ? AND GRUPA = ?";

		try (PreparedStatement ps = veza.prepareStatement(upit)) {

			ps.setString(1, korisnik);
			ps.setString(2, grupa);
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				return true;
			}
		} catch (SQLException ex) {
			Logger.getLogger(UlogeDAO.class.getName()).log(Level.SEVERE, null, ex);
		}
		return false;
	}
}
