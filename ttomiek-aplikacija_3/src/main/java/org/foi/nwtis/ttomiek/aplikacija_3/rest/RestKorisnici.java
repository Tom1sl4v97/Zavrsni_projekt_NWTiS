package org.foi.nwtis.ttomiek.aplikacija_3.rest;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.foi.nwtis.podaci.Korisnik;
import org.foi.nwtis.ttomiek.aplikacija_3.podaci.Grupa;
import org.foi.nwtis.ttomiek.aplikacija_3.podaci.GrupaDAO;
import org.foi.nwtis.ttomiek.aplikacija_3.podaci.KorisniciDAO;
import org.foi.nwtis.ttomiek.aplikacija_3.podaci.TokenDAO;
import org.foi.nwtis.ttomiek.vjezba_06.konfiguracije.bazaPodataka.PostavkeBazaPodataka;

import jakarta.servlet.ServletContext;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Klasa rest korisnici koja pruža rest servise.
 */
@Path("korisnici")
public class RestKorisnici {

	/**
	 * Metoda najprije provjerava ispravnost tokena i dohvaća sve korisnike.
	 *
	 * @param context  parametar contexta
	 * @param korisnik string vrijednost korisničkog imena
	 * @param token    int vrijednost ID-a tokena
	 * @return vraća status odgovora
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	public Response dohvatiSveKorisnike(@Context ServletContext context, @HeaderParam("korisnik") String korisnik,
			@HeaderParam("zeton") int token) {
		Response odgovor = null;
		PostavkeBazaPodataka pbp = (PostavkeBazaPodataka) context.getAttribute("Postavke");
		TokenDAO tdao = new TokenDAO();
		Connection veza = otvoriVezu(pbp);
		boolean provjeraTokena = tdao.provjeriToken(korisnik, token, veza);

		if (provjeraTokena) {
			KorisniciDAO kdao = new KorisniciDAO();
			List<Korisnik> listaSvihKorisnika = kdao.dohvatiSveKorisnike(veza);
			odgovor = Response.status(Response.Status.OK).entity(listaSvihKorisnika).build();
		} else {
			odgovor = Response.status(Response.Status.NOT_FOUND)
					.entity("Nismo pronašli niti jedan aktivan token za korisnika: " + korisnik + "!").build();
		}

		zatvoriVezu(veza);
		return odgovor;
	}

	/**
	 * Metoda otvoara vezu prema bazi podataka.
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
	 * Metoda dodaje novog korisnika u bazu podataka.
	 *
	 * @param context      parametar contexta
	 * @param korisnik     string vrijednost korisničkog imena
	 * @param token        int vrijednost ID-a tokena
	 * @param noviKorisnik podaci novog korisnika
	 * @return vraća status odgovora
	 */
	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	public Response dodajNovogKorisnika(@Context ServletContext context, @HeaderParam("korisnik") String korisnik,
			@HeaderParam("zeton") int token, Korisnik noviKorisnik) {
		Response odgovor = null;
		PostavkeBazaPodataka pbp = (PostavkeBazaPodataka) context.getAttribute("Postavke");
		TokenDAO tdao = new TokenDAO();
		Connection veza = otvoriVezu(pbp);
		boolean provjeraTokena = tdao.provjeriToken(korisnik, token, veza);

		if (provjeraTokena) {
			KorisniciDAO kdao = new KorisniciDAO();
			if (kdao.dodajNovogKorisnika(noviKorisnik, veza)) {
				odgovor = Response.status(Response.Status.OK).entity("OK").build();
			} else {
				odgovor = Response.status(Response.Status.NOT_ACCEPTABLE)
						.entity("Korisničko ime već postoji: " + noviKorisnik.getKorIme() + "!").build();
			}
		} else {
			odgovor = Response.status(Response.Status.NOT_FOUND)
					.entity("Nismo pronašli niti jedan aktivan token za korisnika: " + korisnik + "!").build();
		}

		zatvoriVezu(veza);
		return odgovor;
	}

	/**
	 * Metoda dohvaća traženog korisnika.
	 *
	 * @param context       parametar contexta
	 * @param korisnik      string vrijednost korisničkog imena
	 * @param token         int vrijednost ID-a tokena
	 * @param korisnickoIme string vrijednost korisničkog imena za korisnika kojeg
	 *                      se želi pronaći
	 * @return vraća status odgovora
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("{korisnik}")
	public Response dohvatiKorisnika(@Context ServletContext context, @HeaderParam("korisnik") String korisnik,
			@HeaderParam("zeton") int token, @PathParam("korisnik") String korisnickoIme) {
		Response odgovor = null;
		PostavkeBazaPodataka pbp = (PostavkeBazaPodataka) context.getAttribute("Postavke");
		TokenDAO tdao = new TokenDAO();
		Connection veza = otvoriVezu(pbp);
		boolean provjeraTokena = tdao.provjeriToken(korisnik, token, veza);

		if (provjeraTokena) {
			KorisniciDAO kdao = new KorisniciDAO();
			Korisnik podaciKorisnika = kdao.dohvatiKorisnika(korisnickoIme, veza);
			if (podaciKorisnika != null) {
				odgovor = Response.status(Response.Status.OK).entity(podaciKorisnika).build();
			} else {
				odgovor = Response.status(Response.Status.NOT_FOUND)
						.entity("Nismo pronašli zadanog korisnika: " + korisnickoIme + "!").build();
			}
		} else {
			odgovor = Response.status(Response.Status.NOT_FOUND)
					.entity("Nismo pronašli niti jedan aktivan token za korisnika: " + korisnik + "!").build();
		}

		zatvoriVezu(veza);
		return odgovor;
	}

	/**
	 * Metoda dohvaća podatke o grupama od treženog korisnika.
	 *
	 * @param context       parametar contexta
	 * @param korisnik      string vrijednost korisničkog imena
	 * @param token         int vrijednost ID-a tokena
	 * @param korisnickoIme string vrijednost korisničkog imena za kojeg se želi
	 *                      pronaći grupe
	 * @return vraća status odgovora
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("{korisnik}/grupe")
	public Response dohvatiGrupeKorisnika(@Context ServletContext context, @HeaderParam("korisnik") String korisnik,
			@HeaderParam("zeton") int token, @PathParam("korisnik") String korisnickoIme) {
		Response odgovor = null;

		PostavkeBazaPodataka pbp = (PostavkeBazaPodataka) context.getAttribute("Postavke");
		TokenDAO tdao = new TokenDAO();
		Connection veza = otvoriVezu(pbp);
		boolean provjeraTokena = tdao.provjeriToken(korisnik, token, veza);

		if (provjeraTokena) {
			GrupaDAO gdao = new GrupaDAO();
			List<Grupa> listaGrupe = gdao.dohvatiSveGrupeKorisnika(korisnickoIme, veza);
			if (listaGrupe != null && !listaGrupe.isEmpty()) {
				odgovor = Response.status(Response.Status.OK).entity(listaGrupe).build();
			} else {
				odgovor = Response.status(Response.Status.NOT_FOUND)
						.entity("Nismo pronašli niti jednu grupu u kojoj pripada korisnik: " + korisnickoIme + "!")
						.build();
			}
		} else {
			odgovor = Response.status(Response.Status.NOT_FOUND)
					.entity("Nismo pronašli niti jedan aktivan token za korisnika: " + korisnik + "!").build();
		}

		zatvoriVezu(veza);
		return odgovor;
	}
}
