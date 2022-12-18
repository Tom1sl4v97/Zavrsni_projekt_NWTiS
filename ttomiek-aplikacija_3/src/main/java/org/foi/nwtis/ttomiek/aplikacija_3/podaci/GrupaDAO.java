package org.foi.nwtis.ttomiek.aplikacija_3.podaci;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Klasa za upravljanje bazom podataka za grupe korisnika.
 */
public class GrupaDAO {

	/**
	 * MEtoda dohvaća sve grupe zadanog korisnika.
	 *
	 * @param korisnik podaci korisnika
	 * @param veza     parametar veze
	 * @return vraća listu grupe od zadanog korisnika
	 */
	public List<Grupa> dohvatiSveGrupeKorisnika(String korisnik, Connection veza) {
		String upit = "SELECT g.* FROM GRUPE g INNER JOIN ULOGE u ON g.GRUPA = u.GRUPA WHERE u.KORISNIK = ?";

		try (PreparedStatement ps = veza.prepareStatement(upit)) {

			ps.setString(1, korisnik);
			ResultSet rs = ps.executeQuery();

			List<Grupa> listaGrupe = new ArrayList<Grupa>();

			while (rs.next()) {
				String grupa = rs.getString("GRUPA");
				String naziv = rs.getString("NAZIV");

				Grupa podaciGrupe = new Grupa(grupa, naziv);
				listaGrupe.add(podaciGrupe);
			}
			return listaGrupe;
		} catch (SQLException ex) {
			Logger.getLogger(GrupaDAO.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}
}
