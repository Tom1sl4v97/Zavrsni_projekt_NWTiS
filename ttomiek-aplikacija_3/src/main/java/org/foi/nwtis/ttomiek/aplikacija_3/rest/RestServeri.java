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
import org.foi.nwtis.ttomiek.aplikacija_3.podaci.AerodromiDAO;
import org.foi.nwtis.ttomiek.aplikacija_3.podaci.TokenDAO;
import org.foi.nwtis.ttomiek.vjezba_06.konfiguracije.bazaPodataka.PostavkeBazaPodataka;

import com.google.gson.Gson;

import jakarta.json.Json;
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
 * Klasa rest serveri koji pruža rest servise.
 */
@Path("serveri")
public class RestServeri {

	/**
	 * Metoda najrpije provjerava ispravnost tokena i dohvaća status poslužitelja
	 * app_1.
	 *
	 * @param context  parametar contexta
	 * @param korisnik string vrijednost korisničkog imena
	 * @param token    int vrijednost ID-a tokena
	 * @return vraća status odgovora
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	public Response posaljiStatusPosluzitelju(@Context ServletContext context, @HeaderParam("korisnik") String korisnik,
			@HeaderParam("zeton") int token) {
		Response odgovor = null;
		PostavkeBazaPodataka pbp = (PostavkeBazaPodataka) context.getAttribute("Postavke");
		TokenDAO tdao = new TokenDAO();
		Connection veza = otvoriVezu(pbp);
		boolean provjeraTokena = tdao.provjeriToken(korisnik, token, veza);

		String adresa = pbp.dajPostavku("server.udaljenosti.adresa");
		int port = Integer.parseInt(pbp.dajPostavku("server.udaljenosti.port"));

		if (provjeraTokena) {
			String odgovorPosluzitelja = posaljiStatus(adresa, port, "STATUS");
			String[] odgovorIspravnosti = odgovorPosluzitelja.split(" ");
			if (odgovorIspravnosti[0].equals("ERROR")) {
				odgovor = Response.status(Response.Status.BAD_REQUEST).entity(odgovorPosluzitelja).build();
			} else {
				String json = Json.createObjectBuilder().add("adresa", adresa).add("port", port)
						.add("status", odgovorIspravnosti[1]).build().toString();

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
	 * Metoda vrši komunikaciju sa poslužiteljem app_1 i dohvaća njegov status.
	 *
	 * @param adresa  string vrijednost adrese postlužitelja
	 * @param port    int vrijednost porta postlužitelja
	 * @param komanda string vrijednost komande za izvršavanje postlužitelja
	 * @return vraća string vrijednost odgovora od postlužitelja
	 */
	private String posaljiStatus(String adresa, int port, String komanda) {
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

	/**
	 * Metoda najprije provjerava ispravnost tokena i šalje određenu komandu za
	 * izvršavanje poslužitelja.
	 *
	 * @param context  parametar contexta
	 * @param korisnik string vrijednost korisničkog imena
	 * @param token    int vrijednost ID-a tokena
	 * @param komanda  string vrijednost komande za izvršavanje
	 * @return vraća status odgovora
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("{komanda}")
	public Response posaljiKomanduPosluzitelju(@Context ServletContext context,
			@HeaderParam("korisnik") String korisnik, @HeaderParam("zeton") int token,
			@PathParam("komanda") String komanda) {
		Response odgovor = null;
		PostavkeBazaPodataka pbp = (PostavkeBazaPodataka) context.getAttribute("Postavke");
		TokenDAO tdao = new TokenDAO();
		Connection veza = otvoriVezu(pbp);
		boolean provjeraTokena = tdao.provjeriToken(korisnik, token, veza);

		String adresa = pbp.dajPostavku("server.udaljenosti.adresa");
		int port = Integer.parseInt(pbp.dajPostavku("server.udaljenosti.port"));

		if (provjeraTokena) {
			if (!komanda.equals("QUIT") && !komanda.equals("INIT") && !komanda.equals("CLEAR")) {
				odgovor = Response.status(Response.Status.BAD_REQUEST).entity("Neispravna komanda: " + komanda).build();
			} else {
				String odgovorPosluzitelja = posaljiStatus(adresa, port, komanda);
				String[] odgovorIspravnosti = odgovorPosluzitelja.split(" ");
				if (odgovorIspravnosti[0].equals("ERROR")) {
					odgovor = Response.status(Response.Status.BAD_REQUEST).entity(odgovorPosluzitelja).build();
				} else {
					odgovor = Response.status(Response.Status.OK).entity("OK").build();
				}
			}
		} else {
			odgovor = Response.status(Response.Status.NOT_FOUND)
					.entity("Nismo pronašli niti jedan aktivan token za korisnika: " + korisnik + "!").build();
		}

		zatvoriVezu(veza);
		return odgovor;
	}

	/**
	 * Dodaj novog korisnika.
	 *
	 * @param context  parametar contexta
	 * @param korisnik string vrijednost korisničkog imena
	 * @param token    int vrijednost ID-a tokena
	 * @return vraća status odgovora
	 */
	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	public Response dodajNovogKorisnika(@Context ServletContext context, @HeaderParam("korisnik") String korisnik,
			@HeaderParam("zeton") int token) {
		Response odgovor = null;
		PostavkeBazaPodataka pbp = (PostavkeBazaPodataka) context.getAttribute("Postavke");
		TokenDAO tdao = new TokenDAO();
		Connection veza = otvoriVezu(pbp);
		boolean provjeraTokena = tdao.provjeriToken(korisnik, token, veza);

		String adresa = pbp.dajPostavku("server.udaljenosti.adresa");
		int port = Integer.parseInt(pbp.dajPostavku("server.udaljenosti.port"));

		if (provjeraTokena) {
			AerodromiDAO adao = new AerodromiDAO();
			List<Aerodrom> listaAerodroma = adao.dohvatiSveAerodrome(veza);

			Gson gson = new Gson();
			String json = gson.toJson(listaAerodroma);
			String zahtjev = "LOAD " + json;

			String odgovorPosluzitelja = posaljiStatus(adresa, port, zahtjev);
			String[] odgovorIspravnosti = odgovorPosluzitelja.split(" ");
			int odgovorVelicina = Integer.parseInt(odgovorIspravnosti[1]);
			if (odgovorIspravnosti[0].equals("ERROR")) {
				odgovor = Response.status(Response.Status.CONFLICT).entity(odgovorPosluzitelja).build();
			} else {
				if (odgovorVelicina == listaAerodroma.size()) {
					String jsonOdgovor = Json.createObjectBuilder().add("uspješno", "OK")
							.add("količinaAerodroma", odgovorVelicina).build().toString();
					odgovor = Response.status(Response.Status.OK).entity(jsonOdgovor).build();
				} else {
					odgovor = Response.status(Response.Status.CONFLICT).entity(
							"Trebalo se je ucitati: " + listaAerodroma.size() + " a ucitalo se je: " + odgovorVelicina)
							.build();
				}
			}
		} else {
			odgovor = Response.status(Response.Status.NOT_FOUND)
					.entity("Nismo pronašli niti jedan aktivan token za korisnika: " + korisnik + "!").build();
		}

		zatvoriVezu(veza);
		return odgovor;
	}

}
