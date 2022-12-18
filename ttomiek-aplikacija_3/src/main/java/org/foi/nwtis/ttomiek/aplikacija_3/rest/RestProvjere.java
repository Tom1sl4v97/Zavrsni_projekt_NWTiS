package org.foi.nwtis.ttomiek.aplikacija_3.rest;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.foi.nwtis.podaci.Korisnik;
import org.foi.nwtis.ttomiek.aplikacija_3.podaci.KorisniciDAO;
import org.foi.nwtis.ttomiek.aplikacija_3.podaci.TokenDAO;
import org.foi.nwtis.ttomiek.aplikacija_3.podaci.TokenKorisnika;
import org.foi.nwtis.ttomiek.aplikacija_3.podaci.UlogeDAO;
import org.foi.nwtis.ttomiek.vjezba_06.konfiguracije.bazaPodataka.PostavkeBazaPodataka;

import jakarta.json.Json;
import jakarta.servlet.ServletContext;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Klasa rest provjere koja pruža rest servise.
 */
@Path("provjere")
public class RestProvjere {

	/**
	 * Mtoda najprije provjerava ispravnost prijave korisnika i vraća postojeći
	 * ispravni token ili kreira novi token za korisnika.
	 *
	 * @param context  parametar contexta
	 * @param korisnik string vrijednost korisničkog imena
	 * @param lozinka  string vrijednost lozinke korisnika
	 * @return vraća status odgovora
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	public Response kreirajTokenKorisnika(@Context ServletContext context, @HeaderParam("korisnik") String korisnik,
			@HeaderParam("lozinka") String lozinka) {
		Response odgovor = null;
		PostavkeBazaPodataka pbp = (PostavkeBazaPodataka) context.getAttribute("Postavke");

		KorisniciDAO kdao = new KorisniciDAO();
		Connection veza = otvoriVezu(pbp);
		Korisnik podaciKorisnika = kdao.dohvatiKorisnika(korisnik, veza);

		if (podaciKorisnika != null) {
			if (podaciKorisnika.getLozinka().equals(lozinka)) {

				TokenKorisnika potpuniTK = kreirajNoviToken(korisnik, pbp, veza);

				String json = Json.createObjectBuilder().add("zeton", potpuniTK.getId())
						.add("vrijeme", potpuniTK.getVrijediDo()).build().toString();

				odgovor = Response.status(Response.Status.OK).entity(json).build();
			} else {
				odgovor = Response.status(Response.Status.UNAUTHORIZED)
						.entity("Pogrešna lozinka za korisnika: " + korisnik + "!").build();
			}
		} else {
			odgovor = Response.status(Response.Status.UNAUTHORIZED)
					.entity("Nije pronađeni traženi korisnik: " + korisnik + "!").build();
		}

		zatvoriVezu(veza);
		return odgovor;
	}

	/**
	 * Metoda kreira novi token za korisnika.
	 *
	 * @param korisnik string vrijednost korisničkog imena
	 * @param pbp      parametar za postavke baze podataka
	 * @param veza     parametar veze
	 * @return vraća novokreirani token korisnika
	 */
	private TokenKorisnika kreirajNoviToken(String korisnik, PostavkeBazaPodataka pbp, Connection veza) {

		int trajanjeZetona = Integer.parseInt(pbp.dajPostavku("zeton.trajanje"));
		SimpleDateFormat formatDatuma = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
		Date trenutnoVrijeme = new Date();

		Calendar kalendar = Calendar.getInstance();
		kalendar.setTime(trenutnoVrijeme);
		kalendar.add(Calendar.HOUR, trajanjeZetona);
		Date currentDatePlusOne = kalendar.getTime();

		long epoch = pretvoriDatumEpoch(formatDatuma.format(currentDatePlusOne));
		TokenKorisnika tk = new TokenKorisnika(0, korisnik, 1, epoch);

		TokenDAO tdao = new TokenDAO();
		TokenKorisnika potpuniTK = tdao.provjeriPaDohvati(tk, veza);

		return potpuniTK;
	}

	/**
	 * Metoda otvara vezu prema bazi podataka.
	 *
	 * @param pbp parametar za postavke baze podataka
	 * @return vraća vezu prema bazi podataka
	 */
	private Connection otvoriVezu(PostavkeBazaPodataka pbp) {
		String url = pbp.getServerDatabase() + pbp.getUserDatabase();
		String bpkorisnik = pbp.getUserUsername();
		String bplozinka = pbp.getUserPassword();
		Connection veza = null;
		try {
			Class.forName(pbp.getDriverDatabase(url));
			veza = DriverManager.getConnection(url, bpkorisnik, bplozinka);
		} catch (SQLException | ClassNotFoundException e) {
			Logger.getLogger(RestProvjere.class.getName()).log(Level.SEVERE, null, e);
			e.printStackTrace();
		}
		return veza;
	}

	/**
	 * Metoda zatvara vezu prema bazi podataka.
	 *
	 * @param veza parametar veze
	 */
	private void zatvoriVezu(Connection veza) {
		try {
			veza.close();
		} catch (SQLException e) {
			Logger.getLogger(RestAerodromi.class.getName()).log(Level.SEVERE, null, e);
			e.printStackTrace();
		}
	}

	/**
	 * Metoda pretvara datuma u epoch.
	 *
	 * @param datum vrijednost datuma
	 * @return vraća long parametar epocha
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

	/**
	 * Metoda najprije provjerava ispravnost prijave korisnika i dohvaća traženi
	 * token, ako je ispravan.
	 *
	 * @param context  parametar contexta
	 * @param korisnik string vrijednost korisničkog imena
	 * @param lozinka  string vrijednost lozinke korisnika
	 * @param token    int vrijednost ID-a tokena
	 * @return vraća status odgovora
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("{token}")
	public Response dohvatiToken(@Context ServletContext context, @HeaderParam("korisnik") String korisnik,
			@HeaderParam("lozinka") String lozinka, @PathParam("token") int token) {
		Response odgovor = null;
		PostavkeBazaPodataka pbp = (PostavkeBazaPodataka) context.getAttribute("Postavke");

		KorisniciDAO kdao = new KorisniciDAO();
		Connection veza = otvoriVezu(pbp);
		Korisnik podaciKorisnika = kdao.dohvatiKorisnika(korisnik, veza);

		if (podaciKorisnika != null) {
			if (podaciKorisnika.getLozinka().equals(lozinka)) {
				odgovor = provjeriToken(korisnik, token, "STATUS", veza);
			} else {
				odgovor = Response.status(Response.Status.UNAUTHORIZED)
						.entity("Pogrešna lozinka za korisnika: " + korisnik + "!").build();
			}
		} else {
			odgovor = Response.status(Response.Status.UNAUTHORIZED)
					.entity("Nije pronađeni traženi korisnik: " + korisnik + "!").build();
		}

		zatvoriVezu(veza);
		return odgovor;
	}

	/**
	 * Metoda najprije provjerava ispravnost tokena i odlužuje na temelju izbora da
	 * li će dohvatiti status tokena ili izbrisati navedeni token.
	 *
	 * @param korisnik string vrijednost korisničkog imena
	 * @param token    int virjednost ID-a tokena
	 * @param izbor    string vrijednost izbora o brisanju ili dohvaćanje statusa
	 *                 tokena
	 * @param veza     parametar veze
	 * @return vraća status odgovora
	 */
	private Response provjeriToken(String korisnik, int token, String izbor, Connection veza) {
		TokenDAO tdao = new TokenDAO();
		TokenKorisnika tk = tdao.dohvatiToken(token, veza);
		Response odgovor = null;
		String trenutnoVrijeme = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date());
		long trenutniEpoch = pretvoriDatumEpoch(trenutnoVrijeme);

		if (tk == null) {
			odgovor = Response.status(Response.Status.NOT_FOUND)
					.entity("Ne postoji niti jedan aktivan tokena sa korisnikom: " + korisnik + "!").build();
		} else if (tk.getKorisnik().equals(korisnik)) {
			if (trenutniEpoch <= tk.getVrijediDo()) {
				if (izbor.equals("STATUS")) {
					String json = Json.createObjectBuilder().add("status", tk.getStatus())
							.add("vrijeme", tk.getVrijediDo()).build().toString();

					odgovor = Response.status(Response.Status.OK).entity(json).build();
				}
				if (izbor.equals("DELETE")) {
					tk.setStatus(0);
					tdao.postaviToken(tk, veza);

					String json = Json.createObjectBuilder().add("status", tk.getStatus()).build().toString();

					odgovor = Response.status(Response.Status.OK).entity(json).build();
				}
			} else {
				odgovor = Response.status(Response.Status.REQUEST_TIMEOUT)
						.entity("Navedeni token sa ID: " + tk.getId() + " je istekao!").build();
			}
		} else {
			odgovor = Response.status(Response.Status.UNAUTHORIZED)
					.entity("Traženi token ne odgovara Vašem korisničkom računu: " + korisnik + "!").build();
		}
		return odgovor;
	}

	/**
	 * Metoda najprije provjerava ispravnost prijave korisnika i potom briše
	 * navedeni token, ako je token ispravni i pripada navedenom korisniku.
	 *
	 * @param context  parametar contexta
	 * @param korisnik string vrijednost korisničkog imena
	 * @param lozinka  string vrijednost lozinke korisnika
	 * @param token    int vrijednost ID-a tokena
	 * @return vraća status odgovora
	 */
	@DELETE
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("{token}")
	public Response iskoristiToken(@Context ServletContext context, @HeaderParam("korisnik") String korisnik,
			@HeaderParam("lozinka") String lozinka, @PathParam("token") int token) {
		Response odgovor = null;
		PostavkeBazaPodataka pbp = (PostavkeBazaPodataka) context.getAttribute("Postavke");

		KorisniciDAO kdao = new KorisniciDAO();
		Connection veza = otvoriVezu(pbp);
		Korisnik podaciKorisnika = kdao.dohvatiKorisnika(korisnik, veza);

		if (podaciKorisnika != null) {
			if (podaciKorisnika.getLozinka().equals(lozinka)) {
				odgovor = provjeriToken(korisnik, token, "DELETE", veza);
			} else {
				odgovor = Response.status(Response.Status.UNAUTHORIZED)
						.entity("Pogrešna lozinka za korisnika: " + korisnik + "!").build();
			}
		} else {
			odgovor = Response.status(Response.Status.UNAUTHORIZED)
					.entity("Nije pronađeni traženi korisnik: " + korisnik + "!").build();
		}

		zatvoriVezu(veza);
		return odgovor;
	}

	/**
	 * Metoda najprije provjerava ispravnost prijave korisnika i privilegije
	 * korisnika, te ako korisnik posijeduje privilegije onda se svi važeći tokeni
	 * zadanog korisnika obrišu i postave da su ne važeći.
	 *
	 * @param context       parametar contexta
	 * @param korisnik      string vrijednost korisničkog imena
	 * @param lozinka       string vrijednost lozinke korisnika
	 * @param korisnickoIme string vrijednost korisničkog imena za kojeg se brišeju
	 *                      tokeni
	 * @return vraća status odgovora
	 */
	@DELETE
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("korisnik/{korisnik}")
	public Response iskoristiSveTokeneKorisnika(@Context ServletContext context,
			@HeaderParam("korisnik") String korisnik, @HeaderParam("lozinka") String lozinka,
			@PathParam("korisnik") String korisnickoIme) {
		Response odgovor = null;
		PostavkeBazaPodataka pbp = (PostavkeBazaPodataka) context.getAttribute("Postavke");

		KorisniciDAO kdao = new KorisniciDAO();
		Connection veza = otvoriVezu(pbp);

		Korisnik podaciKorisnika = kdao.dohvatiKorisnika(korisnik, veza);

		if (podaciKorisnika != null) {
			if (podaciKorisnika.getLozinka().equals(lozinka)) {
				UlogeDAO udao = new UlogeDAO();
				boolean provjeraAdmina = udao.provjeriUloguKorisnika(korisnik, "admin", veza);

				if (provjeraAdmina) {
					odgovor = provjeriSveTokeneKorisnika(korisnickoIme, veza);
				} else {
					odgovor = Response.status(Response.Status.UNAUTHORIZED)
							.entity("Niste ovlašteni da provedete traženu akciju!").build();
				}
			} else {
				odgovor = Response.status(Response.Status.UNAUTHORIZED)
						.entity("Pogrešna lozinka za korisnika: " + korisnik + "!").build();
			}
		} else {
			odgovor = Response.status(Response.Status.UNAUTHORIZED)
					.entity("Nije pronađeni traženi korisnik: " + korisnik + "!").build();
		}

		zatvoriVezu(veza);
		return odgovor;
	}

	/**
	 * Metoda provjerava sve tokene traženog korisnika, te ako pronađe jedan ili
	 * više aktivnih tokena, onda ih obriše i postavi da su neaktivni.
	 *
	 * @param korisnickoIme string vrijednost korisničkog imena za kojeg se brišeju
	 *                      tokeni
	 * @param veza          parametar veze
	 * @return vraća status odgovora
	 */
	private Response provjeriSveTokeneKorisnika(String korisnickoIme, Connection veza) {
		TokenDAO tdao = new TokenDAO();
		List<TokenKorisnika> listaTokena = tdao.dohvatiSveTokeneKorisnika(korisnickoIme, veza);
		Response odgovor = null;
		String trenutnoVrijeme = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date());
		long trenutniEpoch = pretvoriDatumEpoch(trenutnoVrijeme);
		boolean aktivanToken = false;

		for (TokenKorisnika tk : listaTokena) {
			if (trenutniEpoch <= tk.getVrijediDo() && tk.getStatus() == 1) {
				tk.setStatus(0);
				tdao.postaviToken(tk, veza);
				aktivanToken = true;
			}
		}
		if (aktivanToken) {
			String json = Json.createObjectBuilder().add("status", "0").build().toString();

			odgovor = Response.status(Response.Status.OK).entity(json).build();
		} else {
			odgovor = Response.status(Response.Status.NOT_FOUND)
					.entity("Traženi korisnik: " + korisnickoIme + " ne posijeduje niti jedan aktivan token!").build();
		}

		return odgovor;
	}
}
