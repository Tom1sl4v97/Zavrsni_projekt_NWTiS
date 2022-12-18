package org.foi.nwtis.ttomiek.aplikacija_5.ws;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.foi.nwtis.podaci.Aerodrom;
import org.foi.nwtis.rest.podaci.AvionLeti;
import org.foi.nwtis.ttomiek.vjezba_06.konfiguracije.bazaPodataka.PostavkeBazaPodataka;

import com.google.gson.Gson;

import jakarta.annotation.Resource;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebService;
import jakarta.servlet.ServletContext;
import jakarta.websocket.WebSocketContainer;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.xml.ws.WebServiceContext;
import jakarta.xml.ws.handler.MessageContext;

/**
 * Klasa WsAerodromi.
 */
@WebService(serviceName = "aerodromi")
public class WsAerodromi {

	/** Parametar web service konteksta. */
	@Resource
	private WebServiceContext wsContext;

	/**
	 * Metoda dohvaća listu svih aerodroma polaska sa formatom datuma dd.MM.yyyy.
	 *
	 * @param korisnik string vrijednost korisničkog imena
	 * @param zeton    string vrijednost ID-a tokena korisnika
	 * @param icao     string vrijednost icao koda aerodroma
	 * @param danOd    string vrijednost za dan od
	 * @param danDo    string vrijednost za dan do
	 * @return vraća listu svih aerodroma polaska
	 */
	@WebMethod
	public List<AvionLeti> dajPolaskeDan(@WebParam(name = "korisnik") String korisnik,
			@WebParam(name = "zeton") String zeton, @WebParam(name = "icao") String icao,
			@WebParam(name = "danOd") String danOd, @WebParam(name = "danDo") String danDo) {

		PostavkeBazaPodataka pbp = dajPBP();
		String konfiguracijskiUrl = pbp.dajPostavku("adresa.app_3");
		String urlAerodroma = odvojiUrl(konfiguracijskiUrl) + "-aplikacija_3/api/aerodromi/" + icao + "/polasci";

		Client client = ClientBuilder.newClient();
		WebTarget webResource = client.target(urlAerodroma).queryParam("vrsta", 0).queryParam("vrijemeOd", danOd)
				.queryParam("vrijemeDo", danDo);

		Response restOdgovor = webResource.request().header("korisnik", korisnik).header("zeton", zeton).get();

		List<AvionLeti> listaPolaska = null;
		if (restOdgovor.getStatus() == 200) {
			String odgovor = restOdgovor.readEntity(String.class);
			Gson gson = new Gson();
			listaPolaska = new ArrayList<AvionLeti>();
			listaPolaska.addAll(Arrays.asList(gson.fromJson(odgovor, AvionLeti[].class)));
		}

		return listaPolaska;
	}

	/**
	 * Metoda odvaja konfiguracijski url u ispravni url.
	 *
	 * @param konfiguracijskiUrl string vrijednost konfiguracijskog url-a
	 * @return vraća string vrijednost ispravnog url-a
	 */
	private String odvojiUrl(String konfiguracijskiUrl) {
		String[] noviUrl = konfiguracijskiUrl.split("/api");
		return noviUrl[0];
	}

	/**
	 * Metoda za dohvaćanje postavki baze podataka.
	 *
	 * @return vraća postavke baze podataka
	 */
	private PostavkeBazaPodataka dajPBP() {
		ServletContext context = (ServletContext) wsContext.getMessageContext().get(MessageContext.SERVLET_CONTEXT);
		PostavkeBazaPodataka pbp = (PostavkeBazaPodataka) context.getAttribute("Postavke");
		return pbp;
	}

	/**
	 * Metoda dohvaća listu svih aerodroma polaska sa formatom datuma u sekundama /
	 * epoch
	 *
	 * @param korisnik  string vrijednost korisničkog imena
	 * @param zeton     string vrijednost ID-a tokena korisnika
	 * @param icao      string vrijednost icao koda aerodroma
	 * @param vrijemeOd string vrijednost za dan od
	 * @param vrijemeDo string vrijednost za dan do
	 * @return vraća listu svih aerodroma polaska
	 */
	@WebMethod
	public List<AvionLeti> dajPolaskeVrijeme(@WebParam(name = "korisnik") String korisnik,
			@WebParam(name = "zeton") String zeton, @WebParam(name = "icao") String icao,
			@WebParam(name = "vrijemeOd") String vrijemeOd, @WebParam(name = "vrijemeDo") String vrijemeDo) {

		PostavkeBazaPodataka pbp = dajPBP();
		String konfiguracijskiUrl = pbp.dajPostavku("adresa.app_3");
		String urlAerodroma = odvojiUrl(konfiguracijskiUrl) + "-aplikacija_3/api/aerodromi/";

		Client client = ClientBuilder.newClient();
		WebTarget webResource = client.target(urlAerodroma).path(icao + "/polasci").queryParam("vrsta", 1)
				.queryParam("vrijemeOd", vrijemeOd).queryParam("vrijemeDo", vrijemeDo);

		Response restOdgovor = webResource.request().header("korisnik", korisnik).header("zeton", zeton).get();

		List<AvionLeti> listaPolaska = null;
		if (restOdgovor.getStatus() == 200) {
			String odgovor = restOdgovor.readEntity(String.class);
			Gson gson = new Gson();
			listaPolaska = new ArrayList<AvionLeti>();
			listaPolaska.addAll(Arrays.asList(gson.fromJson(odgovor, AvionLeti[].class)));
		}

		return listaPolaska;
	}

	/**
	 * Metoda dodaje novi aerodrom za praćenje, no najprije se mora dohvatiti podaci
	 * potrebnog aerodroma.
	 *
	 * @param korisnik string vrijednost korisničkog imena
	 * @param zeton    string vrijednost ID-a tokena korisnika
	 * @param icao     string vrijednost icao koda aerodroma
	 * @return vraća true ako je uspiješno dodan aerodrom za pratiti u bazu, inače
	 *         false
	 */
	@WebMethod
	public boolean dodajAerodromPreuzimanje(@WebParam(name = "korisnik") String korisnik,
			@WebParam(name = "zeton") String zeton, @WebParam(name = "icao") String icao) {

		PostavkeBazaPodataka pbp = dajPBP();
		String konfiguracijskiUrl = pbp.dajPostavku("adresa.app_3");
		String urlAerodroma = odvojiUrl(konfiguracijskiUrl) + "-aplikacija_3/api/aerodromi";

		Client client = ClientBuilder.newClient();
		WebTarget webResourcePost = client.target(urlAerodroma);
		WebTarget webResourceGet = client.target(urlAerodroma + "/" + icao);

		Response restOdgovorGet = webResourceGet.request().header("korisnik", korisnik).header("zeton", zeton).get();

		String jsonAerodroma = obradiOdgovor(restOdgovorGet);
		
		Response restOdgovorPost = webResourcePost.request().header("korisnik", korisnik).header("zeton", zeton)
				.post(Entity.entity(jsonAerodroma, MediaType.APPLICATION_JSON), Response.class);
		if (restOdgovorPost.getStatus() == 200) {
			return true;
		}
		return false;
	}

	/**
	 * Metoda pretvara rest odgovor od poslužitelja u json string aerodroma.
	 *
	 * @param restOdgovor parametar rest odgovora od poslužitelja
	 * @return vraća
	 */
	private String obradiOdgovor(Response restOdgovor) {
		Aerodrom podaciAerodroma = null;
		Gson gson = new Gson();
		if (restOdgovor.getStatus() == 200) {
			String odgovor = restOdgovor.readEntity(String.class);
			podaciAerodroma = gson.fromJson(odgovor, Aerodrom.class);
		}
		return gson.toJson(podaciAerodroma);
	}
}
