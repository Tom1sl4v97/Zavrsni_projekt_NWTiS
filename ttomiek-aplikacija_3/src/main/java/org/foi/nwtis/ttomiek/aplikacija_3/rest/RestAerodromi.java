package org.foi.nwtis.ttomiek.aplikacija_3.rest;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.foi.nwtis.podaci.Aerodrom;
import org.foi.nwtis.rest.podaci.AvionLeti;
import org.foi.nwtis.ttomiek.aplikacija_3.podaci.AerodromiDAO;
import org.foi.nwtis.ttomiek.aplikacija_3.podaci.AerodromiDolasciDAO;
import org.foi.nwtis.ttomiek.aplikacija_3.podaci.AerodromiPolasciDAO;
import org.foi.nwtis.ttomiek.aplikacija_3.podaci.AerodromiPraceniDAO;
import org.foi.nwtis.ttomiek.aplikacija_3.podaci.TokenDAO;
import org.foi.nwtis.ttomiek.vjezba_06.konfiguracije.bazaPodataka.PostavkeBazaPodataka;

import jakarta.json.Json;
import jakarta.servlet.ServletContext;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Klasa rest aerodromi koji pruža rest servise.
 */
@Path("aerodromi")
public class RestAerodromi {

	/**
	 * Metoda najprije provjerava valjanost tokena i zatim dohvaća listu svih
	 * aerodroma.
	 *
	 * @param context  parametar contexta
	 * @param korisnik string vrijednost korisničkog imena
	 * @param token    int vrijednost ID-a tokena
	 * @return vraća status odgovora
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	public Response dohvatiSveAerodrome(@Context ServletContext context, @HeaderParam("korisnik") String korisnik,
			@HeaderParam("zeton") int token, @QueryParam("vrsta") int vrsta) {
		Response odgovor = null;
		PostavkeBazaPodataka pbp = (PostavkeBazaPodataka) context.getAttribute("Postavke");
		Connection veza = otvoriVezu(pbp);

		TokenDAO tdao = new TokenDAO();
		boolean provjeraTokena = tdao.provjeriToken(korisnik, token, veza);

		if (provjeraTokena) {
			List<Aerodrom> listaSvihAerodroma = null;
			if (vrsta == 1) {
				AerodromiPraceniDAO apdao = new AerodromiPraceniDAO();
				listaSvihAerodroma = apdao.dohvatiSvePrateceAerodrome(veza);
			} else {
				AerodromiDAO adao = new AerodromiDAO();
				listaSvihAerodroma = adao.dohvatiSveAerodrome(veza);
			}
			odgovor = Response.status(Response.Status.OK).entity(listaSvihAerodroma).build();
		} else {
			odgovor = Response.status(Response.Status.NOT_FOUND)
					.entity("Nismo pronašli niti jedan aktivan token za korisnika: " + korisnik + "!").build();
		}

		zatvoriVezu(veza);
		return odgovor;
	}

	/**
	 * Metoda otvara vezu prema bazi podataka.
	 *
	 * @param pbp parametar postavki baze podataka
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
			Logger.getLogger(RestAerodromi.class.getName()).log(Level.SEVERE, null, e);
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
	 * Metoda dodaje novi aerodrom u bazu podataka.
	 *
	 * @param context      parametar contexta
	 * @param korisnik     string vrijednost korisničkog imena
	 * @param token        int vrijednost ID-a tokena
	 * @param noviAerodrom podaci novog aerodroma
	 * @return vraća status odgovora
	 */
	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	public Response dodajNoviAerodrom(@Context ServletContext context, @HeaderParam("korisnik") String korisnik,
			@HeaderParam("zeton") int token, Aerodrom noviAerodrom) {
		Response odgovor = null;
		PostavkeBazaPodataka pbp = (PostavkeBazaPodataka) context.getAttribute("Postavke");
		TokenDAO tdao = new TokenDAO();
		Connection veza = otvoriVezu(pbp);
		boolean provjeraTokena = tdao.provjeriToken(korisnik, token, veza);

		if (provjeraTokena) {
			AerodromiPraceniDAO apdao = new AerodromiPraceniDAO();
			String icaoNovogAerodroma = noviAerodrom.getIcao();
			boolean dodajNoviAerodrom = apdao.dodajNovogPracenogAerodroma(icaoNovogAerodroma, veza);
			if (dodajNoviAerodrom) {
				odgovor = Response.status(Response.Status.OK).entity("OK").build();
			} else {
				odgovor = Response.status(Response.Status.NOT_ACCEPTABLE).entity("Navedeni aerodrom: "
						+ icaoNovogAerodroma + " već postoji u tablici ili Ste unijeli nevažeći aerodrom!").build();
			}
		} else {
			odgovor = Response.status(Response.Status.NOT_FOUND)
					.entity("Nismo pronašli niti jedan aktivan token za korisnika: " + korisnik + "!").build();
		}

		zatvoriVezu(veza);
		return odgovor;
	}

	/**
	 * Metoda dohvaća traženi aerodrom.
	 *
	 * @param context  parametar contexta
	 * @param korisnik string vrijednost korisničkog imena
	 * @param token    int vrijednost ID-a tokena
	 * @param icao     string vrijednost icao koda aerodroma
	 * @return vraća status odgovora
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("{icao}")
	public Response dohvatiAerodrom(@Context ServletContext context, @HeaderParam("korisnik") String korisnik,
			@HeaderParam("zeton") int token, @PathParam("icao") String icao) {
		Response odgovor = null;
		PostavkeBazaPodataka pbp = (PostavkeBazaPodataka) context.getAttribute("Postavke");
		TokenDAO tdao = new TokenDAO();
		Connection veza = otvoriVezu(pbp);
		boolean provjeraTokena = tdao.provjeriToken(korisnik, token, veza);

		if (provjeraTokena) {
			AerodromiDAO adao = new AerodromiDAO();
			Aerodrom podaciAerodroma = adao.dohvatiAerodrom(icao, veza);
			odgovor = Response.status(Response.Status.OK).entity(podaciAerodroma).build();
		} else {
			odgovor = Response.status(Response.Status.NOT_FOUND)
					.entity("Nismo pronašli niti jedan aktivan token za korisnika: " + korisnik + "!").build();
		}

		zatvoriVezu(veza);
		return odgovor;
	}

	/**
	 * Metoda najrpije provjerava valjanost tokena i dohvaća sve polaske aerodroma.
	 *
	 * @param context   parametar contexta
	 * @param korisnik  string vrijednost korisničkog imena
	 * @param token     int vrijednost ID-a tokena
	 * @param icao      string vrijednost icao koda aerodroma
	 * @param vrsta     int vrijednost vrste formata datuma
	 * @param vrijemeOd string vrijednost vrijemena od
	 * @param vrijemeDo string vrijednost vrijemena do
	 * @return vraća status odgovora
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("{icao}/polasci")
	public Response dohvatiPolaske(@Context ServletContext context, @HeaderParam("korisnik") String korisnik,
			@HeaderParam("zeton") int token, @PathParam("icao") String icao, @QueryParam("vrsta") int vrsta,
			@QueryParam("vrijemeOd") String vrijemeOd, @QueryParam("vrijemeDo") String vrijemeDo) {
		Response odgovor = null;
		PostavkeBazaPodataka pbp = (PostavkeBazaPodataka) context.getAttribute("Postavke");
		TokenDAO tdao = new TokenDAO();
		Connection veza = otvoriVezu(pbp);
		boolean provjeraTokena = tdao.provjeriToken(korisnik, token, veza);

		if (provjeraTokena) {
			AerodromiPolasciDAO apdao = new AerodromiPolasciDAO();
			List<AvionLeti> listaSvihPolaska = apdao.dohvatiSveAerodromePolaska(veza, icao, vrijemeOd, vrijemeDo,
					vrsta);
			odgovor = Response.status(Response.Status.OK).entity(listaSvihPolaska).build();
		} else {
			odgovor = Response.status(Response.Status.NOT_FOUND)
					.entity("Nismo pronašli niti jedan aktivan token za korisnika: " + korisnik + "!").build();
		}

		zatvoriVezu(veza);
		return odgovor;
	}

	/**
	 * Metoda najprije provjerava valjanost tokena i dohvaća sve dolaske aerodroma.
	 *
	 * @param context   parametar contexta
	 * @param korisnik  string vrijednost korisničkog imena
	 * @param token     int vrijednost ID-a tokena
	 * @param icao      string vrijednost icao koda aerodroma
	 * @param vrsta     int vrijednost vrste formata datuma
	 * @param vrijemeOd string vrijednost vrijemena od
	 * @param vrijemeDo string vrijednost vrijemena do
	 * @return vraća status odgovora
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("{icao}/dolasci")
	public Response dohvatiDolaske(@Context ServletContext context, @HeaderParam("korisnik") String korisnik,
			@HeaderParam("zeton") int token, @PathParam("icao") String icao, @QueryParam("vrsta") int vrsta,
			@QueryParam("vrijemeOd") String vrijemeOd, @QueryParam("vrijemeDo") String vrijemeDo) {
		Response odgovor = null;
		PostavkeBazaPodataka pbp = (PostavkeBazaPodataka) context.getAttribute("Postavke");
		TokenDAO tdao = new TokenDAO();
		Connection veza = otvoriVezu(pbp);
		boolean provjeraTokena = tdao.provjeriToken(korisnik, token, veza);

		if (provjeraTokena) {
			AerodromiDolasciDAO addao = new AerodromiDolasciDAO();
			List<AvionLeti> listaSvihPolaska = addao.dohvatiSveAerodromeDolaska(veza, icao, vrijemeOd, vrijemeDo,
					vrsta);
			odgovor = Response.status(Response.Status.OK).entity(listaSvihPolaska).build();
		} else {
			odgovor = Response.status(Response.Status.NOT_FOUND)
					.entity("Nismo pronašli niti jedan aktivan token za korisnika: " + korisnik + "!").build();
		}

		zatvoriVezu(veza);
		return odgovor;
	}

	/**
	 * Metoda najprije provjerava valjanost tokena i dohvaća udaljenost između dva
	 * aerodroma.
	 *
	 * @param context  parametar contexta
	 * @param korisnik string vrijednost korisničkog imena
	 * @param token    int vrijednost ID-a tokena
	 * @param icao1    string vrijednost prvog icao koda aerodroma
	 * @param icao2    string vrijednost drugog icao koda aerodroma
	 * @return vraća status odgovora
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("{icao1}/{icao2}")
	public Response dohvatiUdaljenost(@Context ServletContext context, @HeaderParam("korisnik") String korisnik,
			@HeaderParam("zeton") int token, @PathParam("icao1") String icao1, @PathParam("icao2") String icao2) {
		Response odgovor = null;
		PostavkeBazaPodataka pbp = (PostavkeBazaPodataka) context.getAttribute("Postavke");
		TokenDAO tdao = new TokenDAO();
		Connection veza = otvoriVezu(pbp);
		boolean provjeraTokena = tdao.provjeriToken(korisnik, token, veza);

		if (provjeraTokena) {
			String komanda = "DISTANCE " + icao1 + " " + icao2;
			String porukaPosluzitelja = dohvatiUdaljenost(pbp, komanda);
			String[] udaljenost = porukaPosluzitelja.split(" ");
			if (udaljenost[0].equals("ERROR")) {
				odgovor = Response.status(Response.Status.NOT_FOUND).entity(porukaPosluzitelja).build();
			} else {
				String json = Json.createObjectBuilder().add("udaljenost", udaljenost[1]).build().toString();
				odgovor = Response.status(Response.Status.OK).entity(json).build();
			}
		} else {
			odgovor = Response.status(Response.Status.NOT_FOUND)
					.entity("Nismo pronašli niti jedan aktivan token za korisnika: " + korisnik + "!").build();
		}

		zatvoriVezu(veza);
		return odgovor;
	}

	/**
	 * Metoda komunicira sa aplikacijom 1, kako bi dohvatila vrijednost udaljenosti
	 * između dva aerodroma.
	 *
	 * @param pbp     parametar postavke baze podataka
	 * @param komanda string vrijednost komande
	 * @return vraća string vrijednost odgovora poslužitelja
	 */
	private String dohvatiUdaljenost(PostavkeBazaPodataka pbp, String komanda) {
		String adresa = pbp.dajPostavku("server.udaljenosti.adresa");
		int port = Integer.parseInt(pbp.dajPostavku("server.udaljenosti.port"));
		try (Socket veza = new Socket(adresa, port);
				InputStreamReader isr = new InputStreamReader(veza.getInputStream(), Charset.forName("UTF-8"));
				OutputStreamWriter osw = new OutputStreamWriter(veza.getOutputStream(), Charset.forName("UTF-8"));) {

			osw.write(komanda);
			osw.flush();
			veza.shutdownOutput();
			StringBuilder tekst = new StringBuilder();
			while (true) {
				int i = isr.read();
				if (i == -1) {
					break;
				}
				tekst.append((char) i);
			}
			veza.shutdownInput();
			veza.close();
			return tekst.toString();
		} catch (SocketException e) {
			return "ERROR 14 Poslužitelj trenutno nije podignuti!";
		} catch (IOException ex) {
			return null;
		}
	}
}
