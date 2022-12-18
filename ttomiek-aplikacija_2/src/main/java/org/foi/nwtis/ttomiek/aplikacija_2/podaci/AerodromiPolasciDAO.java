package org.foi.nwtis.ttomiek.aplikacija_2.podaci;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.foi.nwtis.rest.podaci.AvionLeti;

/**
 * Klasa za upravljanje bazom podataka za aerodrome polaska.
 */
public class AerodromiPolasciDAO {

	/**
	 * Metoda provjerava da li je aerodrom već dodani u bazu.
	 *
	 * @param al   podaci o aerodromu polaska
	 * @param veza parametar veze
	 * @return vraća true ako aerodrom već postoji u bazi, inače false
	 */
	public boolean dohvatiAerodromPolaska(AvionLeti al, Connection veza) {
		String upit = "SELECT ESTDEPARTUREAIRPORT, FIRSTSEEN, LASTSEEN FROM AERODROMI_POLASCI WHERE ESTDEPARTUREAIRPORT = \""
				+ al.getEstDepartureAirport() + "\" AND FIRSTSEEN = " + al.getFirstSeen() + " AND LASTSEEN = "
				+ al.getLastSeen();

		try (Statement s = veza.createStatement(); ResultSet rs = s.executeQuery(upit)) {

			while (rs.next()) {
				if (rs.getString("ESTDEPARTUREAIRPORT").equals(al.getEstDepartureAirport())
						&& rs.getInt("FIRSTSEEN") == al.getFirstSeen() && rs.getInt("LASTSEEN") == al.getLastSeen()) {
					return true;
				}
			}

		} catch (Exception ex) {
			zapisiNoviProblem(al.getEstDepartureAirport(),
					"Problem prilikom čitanja podataka iz tablicu AerodromiPolasci - podaci nisu ispunjeni", veza);
		}
		return false;
	}

	/**
	 * Metoda dodaje novog aerodroma polaska.
	 *
	 * @param listaAerodroma lista aerodroma polaska koji se moraju dodati u bazu
	 * @param veza           parametar veze
	 * @return vraća true ako je uspiješno dodal podatke u bazu, inače false
	 */
	public boolean dodajNovogAerodromaPolaska(List<AvionLeti> listaAerodroma, Connection veza) {
		String upit = "INSERT INTO AERODROMI_POLASCI (ICAO24, FIRSTSEEN, ESTDEPARTUREAIRPORT, LASTSEEN, ESTARRIVALAIRPORT, CALLSIGN, "
				+ "ESTDEPARTUREAIRPORTHORIZDISTANCE, ESTDEPARTUREAIRPORTVERTDISTANCE, ESTARRIVALAIRPORTHORIZDISTANCE, "
				+ "ESTARRIVALAIRPORTVERTDISTANCE, DEPARTUREAIRPORTCANDIDATESCOUNT, ARRIVALAIRPORTCANDIDATESCOUNT, "
				+ "`STORED`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		String trenutniIcao = null;

		DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		LocalDateTime datumVrijeme = LocalDateTime.now();

		try (PreparedStatement s = veza.prepareStatement(upit);) {
			for (int i = 0; i < listaAerodroma.size(); i++) {
				AvionLeti al = listaAerodroma.get(i);
				trenutniIcao = al.getEstDepartureAirport();

				boolean ispravno = dohvatiAerodromPolaska(al, veza);
				if (ispravno) {
					continue;
				}

				s.setString(1, al.getIcao24());
				s.setInt(2, al.getFirstSeen());
				s.setString(3, al.getEstDepartureAirport());
				s.setInt(4, al.getLastSeen());
				s.setString(5, al.getEstArrivalAirport());
				s.setString(6, al.getCallsign());
				s.setInt(7, al.getEstDepartureAirportHorizDistance());
				s.setInt(8, al.getEstDepartureAirportVertDistance());
				s.setInt(9, al.getEstArrivalAirportHorizDistance());
				s.setInt(10, al.getEstArrivalAirportVertDistance());
				s.setInt(11, al.getDepartureAirportCandidatesCount());
				s.setInt(12, al.getArrivalAirportCandidatesCount());
				s.setString(13, format.format(datumVrijeme));
				try {
					s.executeUpdate();
				} catch (Exception e) {
					zapisiNoviProblem(trenutniIcao,
							"Problem prilikom pisanja podataka u tablicu AerodromiPolasci - ICAO podatak polsaka je NULL",
							veza);
				}

			}
			return true;

		} catch (Exception ex) {
			zapisiNoviProblem(trenutniIcao,
					"Problem prilikom pisanja podataka u tablicu AerodromiPolasci - podaci nisu ispunjeni", veza);
		}
		return false;
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
