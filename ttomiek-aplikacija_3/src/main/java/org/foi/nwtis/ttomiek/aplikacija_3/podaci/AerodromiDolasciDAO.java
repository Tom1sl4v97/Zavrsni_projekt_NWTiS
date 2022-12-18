package org.foi.nwtis.ttomiek.aplikacija_3.podaci;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.foi.nwtis.rest.podaci.AvionLeti;

/**
 * Klasa za upravljanje bazom podataka za aerodrome dolaska.
 */
public class AerodromiDolasciDAO {

	/**
	 * Metoda pretvara ulazne datume u ispravni format datuma i dohvaća sve
	 * aerodrome dolaska.
	 *
	 * @param veza      parametar veze
	 * @param icao      string vrijednost icao koda aerdroma
	 * @param vrijemeOd string vrijednost od dana
	 * @param vrijemeDo string vrijednost do dana
	 * @param vrsta     int vrijednost za vrstu formata datuma
	 * @return vraća listu svih aerodroma dolaska
	 */
	public List<AvionLeti> dohvatiSveAerodromeDolaska(Connection veza, String icao, String vrijemeOd, String vrijemeDo,
			int vrsta) {
		long datum1;
		long datum2;

		if (vrsta == 0) {
			datum1 = pretvoriDatumEpoch(vrijemeOd + " 00:00:00") / 1000;
			datum2 = pretvoriDatumEpoch(vrijemeDo + " 23:59:59") / 1000;
		} else {
			datum1 = Long.parseLong(vrijemeOd);
			datum2 = Long.parseLong(vrijemeDo);
		}

		String upit = "SELECT * FROM AERODROMI_DOLASCI WHERE ESTARRIVALAIRPORT = \"" + icao + "\" AND LASTSEEN >= "
				+ datum1 + " AND LASTSEEN <= " + datum2 + " ORDER BY LASTSEEN;";

		List<AvionLeti> sviPolasci = dohvatiDolaskeSaBaze(veza, upit);
		return sviPolasci;
	}

	/**
	 * Metoda dohvaća sve aerodrome dolaska.
	 *
	 * @param veza parametar veze
	 * @param upit string vrijednost za upit koji se provodi nad bazom
	 * @return vraća listu dohvaćenih aerodroma dolaska, inače null
	 */
	public List<AvionLeti> dohvatiDolaskeSaBaze(Connection veza, String upit) {
		List<AvionLeti> avioniPolasci = new ArrayList<>();

		try (Statement s = veza.createStatement(); ResultSet rs = s.executeQuery(upit)) {

			while (rs.next()) {
				String ident = rs.getString("ICAO24");
				int firstSeen = rs.getInt("FIRSTSEEN");
				String estDepartureAirport = rs.getString("ESTDEPARTUREAIRPORT");
				int lastSeen = rs.getInt("LASTSEEN");
				String estArrivalAirport = rs.getString("ESTARRIVALAIRPORT");
				String callsign = rs.getString("CALLSIGN");
				int estDepartureAirportHorizDistance = rs.getInt("ESTDEPARTUREAIRPORTHORIZDISTANCE");
				int estDepartureAirportVertDistance = rs.getInt("ESTDEPARTUREAIRPORTVERTDISTANCE");
				int estArrivalAirportHorizDistance = rs.getInt("ESTARRIVALAIRPORTHORIZDISTANCE");
				int estArrivalAirportVertDistance = rs.getInt("ESTARRIVALAIRPORTVERTDISTANCE");
				int departureAirportCandidatesCount = rs.getInt("DEPARTUREAIRPORTCANDIDATESCOUNT");
				int arrivalAirportCandidatesCount = rs.getInt("ARRIVALAIRPORTCANDIDATESCOUNT");

				AvionLeti av = new AvionLeti(ident, firstSeen, estDepartureAirport, lastSeen, estArrivalAirport,
						callsign, estDepartureAirportHorizDistance, estDepartureAirportVertDistance,
						estArrivalAirportHorizDistance, estArrivalAirportVertDistance, departureAirportCandidatesCount,
						arrivalAirportCandidatesCount);
				avioniPolasci.add(av);
			}
			return avioniPolasci;

		} catch (SQLException ex) {
			Logger.getLogger(AerodromiDolasciDAO.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	/**
	 * Metoda pretvara datum u epoch vrijednost.
	 *
	 * @param datum parametar unesenog datuma korisnika
	 * @return vraća long parametar epoch
	 */
	private long pretvoriDatumEpoch(String datum) {
		long epoch = -1;
		try {
			epoch = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").parse(datum).getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return epoch;
	}
}
