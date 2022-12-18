package org.foi.nwtis.ttomiek.aplikacija_2.podaci;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Klasa za upravljanje bazom podataka za probleme aerodroma.
 */
public class AerodromiProblemiDAO {

	/**
	 * Metoda dodaje novi problem.
	 *
	 * @param ap   podaci o problemu aerodroma
	 * @param veza parametar veze
	 * @return vraća true ako je uspiješno dado u bazu, inače false
	 */
	public boolean dodajNoviProblem(AerodromiProblemi ap, Connection veza) {
		String upit = "INSERT INTO AERODROMI_PROBLEMI (IDENT, DESCRIPTION, `STORED`) VALUES (?, ?, ?)";

		try (PreparedStatement s = veza.prepareStatement(upit)) {

			s.setString(1, ap.getIdent());
			s.setString(2, ap.getDescription());
			s.setString(3, ap.getStored());

			int brojAzuriranja = s.executeUpdate();

			return brojAzuriranja == 1;

		} catch (Exception ex) {
			Logger.getLogger(AerodromiProblemiDAO.class.getName()).log(Level.SEVERE, null, ex);
		}
		return false;
	}

}
