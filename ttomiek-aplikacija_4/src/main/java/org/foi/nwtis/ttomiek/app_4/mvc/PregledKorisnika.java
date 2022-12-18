package org.foi.nwtis.ttomiek.app_4.mvc;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.foi.nwtis.podaci.Korisnik;
import org.foi.nwtis.ttomiek.app_4.podaci.TokenKorisnika;
import org.foi.nwtis.ttomiek.vjezba_06.konfiguracije.bazaPodataka.PostavkeBazaPodataka;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.mvc.Controller;
import jakarta.mvc.Models;
import jakarta.mvc.View;
import jakarta.servlet.ServletContext;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;

/**
 * Klasa pregled korisnika koja služi za dobivanje potrebnih informacija za
 * prikaz na jsp stranicama.
 */
@Controller
@Path("korisnici")
@RequestScoped
public class PregledKorisnika {

	/** Model. */
	@Inject
	private Models model;

	/**
	 * Metoda vrši prikaz početne stranice.
	 */
	@GET
	@Path("pocetak")
	@View("index.jsp")
	public void pocetak() {
	}

	/**
	 * Metoda vrši prikaz stranice za registraciju.
	 */
	@GET
	@Path("registracijaKorisnikaUnos")
	@View("registracijaKorisnikaUnos.jsp")
	public void registrirajKorisnikaUnos() {
	}

	/**
	 * Metoda koja se vrši nakon što korisnik pritisne gumb registriraj me, na
	 * stranici registracije korisnika.
	 *
	 * @param context parametar contexta
	 * @param korime  string vrijednost korisničkog imena
	 * @param lozinka string vrijednost lozinke
	 * @param ime     string vrijednost imena korisnika
	 * @param prezime string vrijednost prezimena korisnika
	 * @param email   string vrijednost emaila korisnika
	 */
	@POST
	@Path("registracijaKorisnika")
	@View("registracijaKorisnikaUnos.jsp")
	public void registrirajKorisnika(@Context ServletContext context, @FormParam("korime") String korime,
			@FormParam("lozinka") String lozinka, @FormParam("ime") String ime, @FormParam("prezime") String prezime,
			@FormParam("email") String email) {
		PostavkeBazaPodataka pbp = (PostavkeBazaPodataka) context.getAttribute("Postavke");
		Korisnik noviKorisnik = new Korisnik(korime, ime, prezime, lozinka, email);
		KorisniciKlijent kk = new KorisniciKlijent();

		String odgovor;
		if (kk.registrirajKorisnika(noviKorisnik, pbp)) {
			odgovor = "Uspiješna registracija!";
		} else {
			odgovor = "Neuspiješna registracija, korisničko ime je Već zauzeto!";
		}

		model.put("odgovor", odgovor);
	}

	/**
	 * Metoda vrši prikaz stranice za prijavu.
	 */
	@GET
	@Path("prijavaKorisnikaUnos")
	@View("prijavaKorisnikaUnos.jsp")
	public void prijaviKorisnikaUnos() {
	}

	/**
	 * Metoda koja se vrši nakon što korisnik pritisne gumb prijavi me, na stranici
	 * prijave korisnika.
	 *
	 * @param context parametar contexta
	 * @param korime  string vrijednost korisničkog imena
	 * @param lozinka string vrijednost lozinke
	 */
	@POST
	@Path("prijavaKorisnika")
	@View("prijavaKorisnikaUnos.jsp")
	public void prijaviKorisnika(@Context ServletContext context, @FormParam("korime") String korime,
			@FormParam("lozinka") String lozinka) {
		PostavkeBazaPodataka pbp = (PostavkeBazaPodataka) context.getAttribute("Postavke");
		KorisniciKlijent kk = new KorisniciKlijent();

		TokenKorisnika tokenKorisnika = kk.prijavaKorisnika(korime, lozinka, pbp);

		String odgovor = "Korisnički podaci nisu valjani, molimo pokušajte ponovno!";
		if (tokenKorisnika != null) {
			odgovor = "Uspiješno ste se ulogirali!";
			context.setAttribute("tokenKorisnika", tokenKorisnika);
		}

		model.put("odgovor", odgovor);
	}

	/**
	 * Metoda vrši prikaz stranice za pregled korisnika, te korisnik mora biti
	 * prijavljeni radi tokena korisnika.
	 *
	 * @param context parametar contexta
	 * @return vraća string vrijednost ako korisnik nije prijavljeni, te prebacuje
	 *         korisnika na stranicu za prijavu, inače vraća null
	 */
	@GET
	@Path("pregledKorisnika")
	@View("pregledKorisnika.jsp")
	public String pregledKorisnika(@Context ServletContext context) {
		PostavkeBazaPodataka pbp = (PostavkeBazaPodataka) context.getAttribute("Postavke");
		TokenKorisnika tokenKorisnika = dohvatiTokenKorisnika(context);
		if (tokenKorisnika == null) {
			return "redirect:korisnici/prijavaKorisnikaUnos";
		}
		KorisniciKlijent kk = new KorisniciKlijent();
		List<Korisnik> listaKorisnika = kk.dohvatiKorisnike(tokenKorisnika, pbp);

		boolean privilegijeAdmina = kk.provjeriPrivilegijeAdmina(tokenKorisnika, pbp);
		if (privilegijeAdmina) {
			model.put("privilegijeAdmina", true);
		} else {
			model.put("privilegijeAdmina", false);
		}

		model.put("listaKorisnika", listaKorisnika);
		return null;
	}

	/**
	 * Metoda dohvaća token korisnika iz contexta.
	 *
	 * @param context parametar contexta
	 * @return vraća token prijavljenog korisnika
	 */
	private TokenKorisnika dohvatiTokenKorisnika(ServletContext context) {
		TokenKorisnika tokenKorisnika = (TokenKorisnika) context.getAttribute("tokenKorisnika");
		if (tokenKorisnika != null) {
			if (provjeriTokenKorisnika(tokenKorisnika)) {
				return tokenKorisnika;
			}
		}
		return null;
	}

	/**
	 * Metoda provjerava ispravnost tokena prijavljenog korisnika.
	 *
	 * @param tokenKorisnika podaci tokena korisnika
	 * @return vraća true ako je token ispravan, inače false
	 */
	private boolean provjeriTokenKorisnika(TokenKorisnika tokenKorisnika) {
		long epoch = -1;
		String trenutnoVrijeme = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date());
		try {
			epoch = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").parse(trenutnoVrijeme).getTime();
			if (epoch <= tokenKorisnika.getVrijediDo()) {
				return true;
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * Metoda koja se vrši nakon što korisnik pritisne gumb obriši vlastiti token,
	 * na stranici pregleda korisnika.
	 *
	 * @param context parametar contexta
	 * @return vraća string vrijednost ako korisnik nije prijavljeni, te prebacuje
	 *         korisnika na stranicu za prijavu, inače vraća null
	 */
	@POST
	@Path("obrisiVlastitiToken")
	@View("index.jsp")
	public String obrisiVlastitiToken(@Context ServletContext context) {
		PostavkeBazaPodataka pbp = (PostavkeBazaPodataka) context.getAttribute("Postavke");

		TokenKorisnika tokenKorisnika = dohvatiTokenKorisnika(context);

		KorisniciKlijent kk = new KorisniciKlijent();
		String odgovor = kk.obrisiVlastitiToken(tokenKorisnika, pbp);

		context.setAttribute("tokenKorisnika", null);
		context.setAttribute("prijavljeniKorisnik", null);

		if (odgovor == null) {
			return "redirect:korisnici/pocetak";
		}

		return null;
	}

	/**
	 * Metoda koja se vrši nakon što korisnik pritisne određeni gumb obriši token
	 * nekog korisnika, na stranici pregleda korisnika. Ako prijavljeni korisnik ima
	 * privilegije admina onda se prikažu navedeni gumbovi, inače su sakriveni ako
	 * korisnik ne posijeduje privilegije admina. Gumbovi brišu sve aktivne tokene
	 * zadanog korisnika.
	 *
	 * @param context parametar contexta
	 * @param korime  string vrijednost koriničkog imena za brisanje tokena
	 * @return vraća string vrijednost ako korisnik odabere vlastiti gumb za
	 *         brisanje tokena korisnika iz popisa korisnika, te prebacuje korisnika
	 *         na početnu stranicu, inače vraća null
	 */
	@POST
	@Path("obrisiTokenKorisnika")
	@View("pregledKorisnika.jsp")
	public String obrisiTokenKorisnika(@Context ServletContext context, @FormParam("korime") String korime) {
		PostavkeBazaPodataka pbp = (PostavkeBazaPodataka) context.getAttribute("Postavke");

		TokenKorisnika tokenKorisnika = dohvatiTokenKorisnika(context);

		KorisniciKlijent kk = new KorisniciKlijent();
		String odgovor = kk.obrisiTokenKorisnika(tokenKorisnika, korime, pbp);

		if (odgovor == null) {
			odgovor = "Uspiješno smo obrisali token/tokene od korisnika: " + korime;
		}

		if (korime.equals(tokenKorisnika.getKorisnik())) {
			return "redirect:korisnici/pocetak";
		}

		List<Korisnik> listaKorisnika = kk.dohvatiKorisnike(tokenKorisnika, pbp);
		model.put("privilegijeAdmina", true);
		model.put("listaKorisnika", listaKorisnika);
		model.put("odgovor", odgovor);
		return null;
	}

	/**
	 * Metoda vrši prikaz stranice za upravljanjem poslužiteljem koja dohvaća STATUS
	 * poslužitelja, te korisnik mora biti prijavljeni radi tokena korisnika.
	 *
	 * @param context parametar contexta
	 * @return vraća string vrijednost ako korisnik nije prijavljeni, te prebacuje
	 *         korisnika na stranicu za prijavu, inače vraća null
	 */
	@GET
	@Path("upravljanjePosluziteljem")
	@View("upravljanjePosluziteljem.jsp")
	public String upravljanjePosluziteljem(@Context ServletContext context) {
		PostavkeBazaPodataka pbp = (PostavkeBazaPodataka) context.getAttribute("Postavke");
		TokenKorisnika tokenKorisnika = dohvatiTokenKorisnika(context);
		if (tokenKorisnika == null) {
			return "redirect:korisnici/prijavaKorisnikaUnos";
		}

		String adresa = pbp.dajPostavku("server.udaljenosti.adresa");
		int port = Integer.parseInt(pbp.dajPostavku("server.udaljenosti.port"));
		KorisniciKlijent kk = new KorisniciKlijent();
		String odgovor = kk.dohvatiOdgovorPosluzitelja("STATUS", adresa, port);

		model.put("status", odgovor);
		return null;
	}

	/**
	 * Metoda koja se vrši nakon što korisnik pošalje odabranu naredbu za
	 * upravljanje poslužiteljem. Korisnik mora biti prijavljeni radi tokena
	 * korinika.
	 *
	 * @param context parametar contexta
	 * @param odabir  string vrijednost naredbe za uspravljanje poslužiteljem
	 * @return vraća string vrijednost ako korisnik nije prijavljeni, te prebacuje
	 *         korisnika na oredeđenu stranicu, inače vraća null
	 */
	@POST
	@Path("upravljanjePosluziteljemNaredba")
	@View("upravljanjePosluziteljem.jsp")
	public String upravljanjePosluziteljemKomanda(@Context ServletContext context, @FormParam("odabir") String odabir) {
		PostavkeBazaPodataka pbp = (PostavkeBazaPodataka) context.getAttribute("Postavke");
		TokenKorisnika tokenKorisnika = dohvatiTokenKorisnika(context);
		if (tokenKorisnika == null) {
			return "redirect:korisnici/prijavaKorisnikaUnos";
		}
		KorisniciKlijent kk = new KorisniciKlijent();
		if (odabir.equals("LOAD")) {
			odabir += " " + kk.dohvatiListuAerodroma(tokenKorisnika, pbp);
		}

		String adresa = pbp.dajPostavku("server.udaljenosti.adresa");
		int port = Integer.parseInt(pbp.dajPostavku("server.udaljenosti.port"));
		String odgovor = kk.dohvatiOdgovorPosluzitelja(odabir, adresa, port);
		String odgovorStatus = kk.dohvatiOdgovorPosluzitelja("STATUS", adresa, port);

		if (odabir.equals("QUIT")) {
			return "redirect:korisnici/upravljanjePosluziteljem";
		}

		model.put("status", odgovorStatus);
		model.put("odgovorServera", odgovor);
		return null;
	}
}
