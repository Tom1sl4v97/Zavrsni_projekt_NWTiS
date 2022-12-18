package org.foi.nwtis.ttomiek.aplikacija_3.podaci;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.foi.nwtis.podaci.Korisnik;

/**
 * Klasa za upravljanje bazom podataka za korisnike.
 */
public class KorisniciDAO {

	/**
	 * Metoda dohvaća korisnika, na temelju njegovog korisničkog imena.
	 *
	 * @param korisnik string vrijednost korisničkog imena
	 * @param veza     parametar veze
	 * @return vraća podatke pronađenog korisnika
	 */
	public Korisnik dohvatiKorisnika(String korisnik, Connection veza) {
		String upit = "SELECT * FROM KORISNICI WHERE korisnik = ?";

		try (PreparedStatement ps = veza.prepareStatement(upit)) {

			ps.setString(1, korisnik);
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				String ime = rs.getString("IME");
				String prezime = rs.getString("PREZIME");
				String lozinka = rs.getString("LOZINKA");
				String email = rs.getString("EMAIL");

				Korisnik dohvaceniKorisnik = new Korisnik(korisnik, ime, prezime, lozinka, email);
				return dohvaceniKorisnik;
			}

		} catch (SQLException ex) {
			Logger.getLogger(KorisniciDAO.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	/**
	 * Metoda dohvaća sve korisnike.
	 *
	 * @param veza parametar veze
	 * @return vraća listu svih korisnika
	 */
	public List<Korisnik> dohvatiSveKorisnike(Connection veza) {
		String upit = "SELECT * FROM KORISNICI";

		try (PreparedStatement ps = veza.prepareStatement(upit); ResultSet rs = ps.executeQuery()) {

			List<Korisnik> listaSvihKorisnika = new ArrayList<Korisnik>();

			while (rs.next()) {
				String korisnik = rs.getString("KORISNIK");
				String ime = rs.getString("IME");
				String prezime = rs.getString("PREZIME");
				String lozinka = rs.getString("LOZINKA");
				String email = rs.getString("EMAIL");

				Korisnik dohvaceniKorisnik = new Korisnik(korisnik, ime, prezime, lozinka, email);
				listaSvihKorisnika.add(dohvaceniKorisnik);
			}
			return listaSvihKorisnika;
		} catch (SQLException ex) {
			Logger.getLogger(KorisniciDAO.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	/**
	 * Metoda dodaje novog korisnika u bazu.
	 *
	 * @param podaciKorisnika podaci novog korisnika
	 * @param veza            parametar veze
	 * @return vraća true ako je uspiješno dodal novog korisnika, inače false
	 */
	public boolean dodajNovogKorisnika(Korisnik podaciKorisnika, Connection veza) {
		String korisnickoIme = podaciKorisnika.getKorIme();

		if (this.dohvatiKorisnika(korisnickoIme, veza) != null) {
			return false;
		}

		String upit = "INSERT INTO KORISNICI VALUES (?, ?, ?, ?, ?)";

		try (PreparedStatement s = veza.prepareStatement(upit)) {

			s.setString(1, korisnickoIme);
			s.setString(2, podaciKorisnika.getIme());
			s.setString(3, podaciKorisnika.getPrezime());
			s.setString(4, podaciKorisnika.getLozinka());
			s.setString(5, podaciKorisnika.getEmail());

			int brojAzuriranja = s.executeUpdate();

			return brojAzuriranja == 1;

		} catch (Exception ex) {
			Logger.getLogger(KorisniciDAO.class.getName()).log(Level.SEVERE, null, ex);
		}
		return false;
	}
}
