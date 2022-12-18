package org.foi.nwtis.ttomiek.aplikacija_3.podaci;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Klasa za upravljanje bazom podataka za korisnikove tokene.
 */
public class TokenDAO {

	/**
	 * Metoda provjerava da li korisnik posijeduje ispravni token ili ne, ako ne
	 * posijeduje ispravni token onda se kreira novi token za korisnika i vraća mu
	 * se novo kreirani token, a ako već postoji valjanji token onda mu se taj
	 * valjani token vraća.
	 *
	 * @param tk   podaci tokena korisnika
	 * @param veza parametar veze
	 * @return vraća se podaci o tokenu korisnika
	 */
	public TokenKorisnika provjeriPaDohvati(TokenKorisnika tk, Connection veza) {
		List<TokenKorisnika> listaTokena = dohvatiSveTokeneKorisnika(tk.getKorisnik(), veza);
		TokenKorisnika potpuniToken = null;
		if (listaTokena == null || listaTokena.isEmpty()) {
			kreirajNoviToken(tk, veza);
			potpuniToken = dohvatiTokenKorisnika(tk, veza);
		} else {
			potpuniToken = listaTokena.get(0);
		}
		return potpuniToken;
	}

	/**
	 * Metoda kreira novi token za korisnika.
	 *
	 * @param tk   podaci tokena korisnika
	 * @param veza parametar veze
	 * @return vraća true ako se je token uspiješno pohranio u bazu, inače false
	 */
	private boolean kreirajNoviToken(TokenKorisnika tk, Connection veza) {
		String upit = "INSERT INTO TOKEN (KORISNIK, STATUS, VRIJEDIDO) VALUES (?,?,?)";

		try (PreparedStatement ps = veza.prepareStatement(upit)) {

			ps.setString(1, tk.getKorisnik());
			ps.setInt(2, tk.getStatus());
			ps.setLong(3, tk.getVrijediDo());

			ps.executeUpdate();
			return true;

		} catch (SQLException ex) {
			Logger.getLogger(TokenDAO.class.getName()).log(Level.SEVERE, null, ex);
		}
		return false;
	}

	/**
	 * Metoda dohvaća novo kreirani token korisnika, iz razloga zato što se trenutno
	 * ne posijeduje informacija o ID-ju tokena (baza podataka to auto generira).
	 *
	 * @param tk   podaci tokena korisnika
	 * @param veza parametar veze
	 * @return vraća podatke o tokenu korisnika
	 */
	private TokenKorisnika dohvatiTokenKorisnika(TokenKorisnika tk, Connection veza) {
		String upit = "SELECT * FROM TOKEN WHERE KORISNIK = ? AND STATUS = ? AND VRIJEDIDO = ?";

		try (PreparedStatement ps = veza.prepareStatement(upit)) {

			ps.setString(1, tk.getKorisnik());
			ps.setInt(2, tk.getStatus());
			ps.setLong(3, tk.getVrijediDo());
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				int id = Integer.parseInt(rs.getString("ID"));
				String korisnickoIme = rs.getString("KORISNIK");
				int status = rs.getInt("STATUS");
				long vrijediDo = rs.getLong("VRIJEDIDO");

				TokenKorisnika potpuniTK = new TokenKorisnika(id, korisnickoIme, status, vrijediDo);
				return potpuniTK;
			}
		} catch (SQLException ex) {
			Logger.getLogger(TokenDAO.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	/**
	 * Metoda dohvaća token korisnika.
	 *
	 * @param id   int vrijednost od ID-a tokena
	 * @param veza parametar veze
	 * @return vraća podatke tokena korisnika
	 */
	public TokenKorisnika dohvatiToken(int id, Connection veza) {
		String upit = "SELECT * FROM TOKEN WHERE ID = ? AND STATUS = 1";

		try (PreparedStatement ps = veza.prepareStatement(upit)) {

			ps.setInt(1, id);
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				String korisnickoIme = rs.getString("KORISNIK");
				int status = rs.getInt("STATUS");
				long vrijediDo = rs.getLong("VRIJEDIDO");

				TokenKorisnika potpuniTK = new TokenKorisnika(id, korisnickoIme, status, vrijediDo);
				return potpuniTK;
			}
		} catch (SQLException ex) {
			Logger.getLogger(TokenDAO.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	/**
	 * Metoda ažurira token korisnika.
	 *
	 * @param tk   podaci tokena korisnika
	 * @param veza parametar veze
	 */
	public void postaviToken(TokenKorisnika tk, Connection veza) {
		String upit = "UPDATE TOKEN SET STATUS = ? WHERE ID = ?";

		try (PreparedStatement ps = veza.prepareStatement(upit)) {

			ps.setInt(1, tk.getStatus());
			ps.setInt(2, tk.getId());

			ps.executeUpdate();

		} catch (SQLException ex) {
			Logger.getLogger(TokenDAO.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/**
	 * Metoda dohvaća sve tokene korisnika.
	 *
	 * @param korisnickoIme string vrijednost korisničkog imena
	 * @param veza          parametar veze
	 * @return vraća listu svih valjanih tokena korisnika
	 */
	public List<TokenKorisnika> dohvatiSveTokeneKorisnika(String korisnickoIme, Connection veza) {
		String upit = "SELECT * FROM TOKEN WHERE KORISNIK = ? AND STATUS = 1 AND VRIJEDIDO >= ? ORDER BY VRIJEDIDO";

		String trenutnoVrijeme = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date());
		long trenutniEpoch = pretvoriDatumEpoch(trenutnoVrijeme);

		try (PreparedStatement ps = veza.prepareStatement(upit)) {

			ps.setString(1, korisnickoIme);
			ps.setLong(2, trenutniEpoch);
			ResultSet rs = ps.executeQuery();

			List<TokenKorisnika> listaTokena = new ArrayList<TokenKorisnika>();

			while (rs.next()) {
				int id = rs.getInt("ID");
				int status = rs.getInt("STATUS");
				long vrijediDo = rs.getLong("VRIJEDIDO");

				TokenKorisnika potpuniTK = new TokenKorisnika(id, korisnickoIme, status, vrijediDo);
				listaTokena.add(potpuniTK);
			}

			return listaTokena;
		} catch (SQLException ex) {
			Logger.getLogger(TokenDAO.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	/**
	 * Metoda provjerava ispravnost tokena korisnika.
	 *
	 * @param korisnik string vrijednost korisničkog imena
	 * @param token    int vrijednost ID-a tokena
	 * @param veza     parametar veze
	 * @return vraća true ako je navedeni token ispravan, inače false
	 */
	public boolean provjeriToken(String korisnik, int token, Connection veza) {
		String trenutnoVrijeme = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date());
		long trenutniEpoch = pretvoriDatumEpoch(trenutnoVrijeme);

		String upit = "SELECT * FROM TOKEN WHERE KORISNIK = ? AND ID = ? AND VRIJEDIDO >= ? AND STATUS = 1";

		try (PreparedStatement ps = veza.prepareStatement(upit)) {

			ps.setString(1, korisnik);
			ps.setInt(2, token);
			ps.setLong(3, trenutniEpoch);
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				return true;
			}
		} catch (SQLException ex) {
			Logger.getLogger(TokenDAO.class.getName()).log(Level.SEVERE, null, ex);
		}
		return false;
	}

	/**
	 * Metoda za pretvaranje datuma u epoch.
	 *
	 * @param datumKonfig datum iz konfiguracijske datoteke
	 * @return vraća long parametar
	 */
	private long pretvoriDatumEpoch(String datumKonfig) {
		long epoch = -1;
		try {
			epoch = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").parse(datumKonfig).getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return epoch;
	}
}
