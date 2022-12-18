package org.foi.nwtis.ttomiek.aplikacija_5.ws;

import org.foi.nwtis.podaci.Aerodrom;
import org.foi.nwtis.rest.klijenti.NwtisRestIznimka;
import org.foi.nwtis.rest.klijenti.OWMKlijent;
import org.foi.nwtis.rest.podaci.Lokacija;
import org.foi.nwtis.rest.podaci.MeteoPodaci;
import org.foi.nwtis.ttomiek.aplikacija_5.wsock.Info;
import org.foi.nwtis.ttomiek.vjezba_06.konfiguracije.bazaPodataka.PostavkeBazaPodataka;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import jakarta.annotation.Resource;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebService;
import jakarta.servlet.ServletContext;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;
import jakarta.xml.ws.WebServiceContext;
import jakarta.xml.ws.handler.MessageContext;

/**
 * Klasa WsMeteo.
 */
@WebService(serviceName = "meteo")
public class WsMeteo {

	/** Parametar za kontekst web servleta. */
	@Resource
	private WebServiceContext wsContext;

	/**
	 * Web metoda za dohvaćanje meteo podataka.
	 *
	 * @param icao string vrijednost icao koda aerodroma
	 * @return vraća meteo podatke aerodroma
	 */
	@WebMethod
	public MeteoPodaci dajMeteo(@WebParam(name = "icao") String icao) {
		PostavkeBazaPodataka pbp = dajPBP();

		Response restOdgovor = dohvatiOdgovorAerodromi(pbp, icao);

		Aerodrom podaciAerodroma = null;
		Gson gson = new Gson();
		if (restOdgovor.getStatus() == 200) {
			String odgovor = restOdgovor.readEntity(String.class);
			podaciAerodroma = gson.fromJson(odgovor, Aerodrom.class);
		}

		if (podaciAerodroma == null)
			return null;
		Lokacija lokacijaAerodroma = podaciAerodroma.getLokacija();
		System.out.println("LOKACIJA: " + lokacijaAerodroma.getLatitude() + " i " + lokacijaAerodroma.getLongitude());

		String key = pbp.dajPostavku("OpenWeatherMap.apikey");
		OWMKlijent owk = new OWMKlijent(key);
		MeteoPodaci meteoPodaciAerodroma = null;
		try {
			meteoPodaciAerodroma = owk.getRealTimeWeather(lokacijaAerodroma.getLatitude(),
					lokacijaAerodroma.getLongitude());
		} catch (NwtisRestIznimka e) {
			e.printStackTrace();
		}
		
		Info.informiraj("info");

		return meteoPodaciAerodroma;
	}

	/**
	 * Metoda najprije dohvaća potreban token korisnika kako bi se dohvatili
	 * potrebni podaci o treženom aerodromu.
	 *
	 * @param pbp  parametar za postavke baze podataka
	 * @param icao string vrijednost icao koda aerodroma
	 * @return vraća status odgovora
	 */
	private Response dohvatiOdgovorAerodromi(PostavkeBazaPodataka pbp, String icao) {
		String korIme = pbp.dajPostavku("sustav.korisnik");
		String lozinka = pbp.dajPostavku("sustav.lozinka");

		String konfiguracijskiUrl = pbp.dajPostavku("adresa.app_3");
		String urlProvjere = odvojiUrl(konfiguracijskiUrl) + "-aplikacija_3/api/provjere";

		Client client = ClientBuilder.newClient();
		WebTarget webResourceProvjere = client.target(urlProvjere);

		Response restOdgovorProvjere = webResourceProvjere.request().header("korisnik", korIme)
				.header("lozinka", lozinka).get();

		int idTokena = -1;
		if (restOdgovorProvjere.getStatus() == 200) {
			String odgovor = restOdgovorProvjere.readEntity(String.class);
			JsonObject json = new Gson().fromJson(odgovor, JsonObject.class);
			idTokena = json.get("zeton").getAsInt();
		}

		if (idTokena > 0) {
			String urlAerodroma = odvojiUrl(konfiguracijskiUrl) + "-aplikacija_3/api/aerodromi/" + icao;
			WebTarget webResourceAerodromi = client.target(urlAerodroma);
			Response restOdgovorAerodromi = webResourceAerodromi.request().header("korisnik", korIme)
					.header("zeton", idTokena).get();
			return restOdgovorAerodromi;
		}

		return null;
	}

	/**
	 * Metoda dohvaća postavke baze podataka.
	 *
	 * @return vraća postavke baze podataka
	 */
	private PostavkeBazaPodataka dajPBP() {
		ServletContext context = (ServletContext) wsContext.getMessageContext().get(MessageContext.SERVLET_CONTEXT);
		PostavkeBazaPodataka pbp = (PostavkeBazaPodataka) context.getAttribute("Postavke");
		return pbp;
	}

	/**
	 * Metoda odvaja konfiguracijski url u potreban url.
	 *
	 * @param konfiguracijskiUrl string vrijednost konfiguracijskog url-a
	 * @return vraća string vrijednost ispravnog url-a
	 */
	private String odvojiUrl(String konfiguracijskiUrl) {
		String[] noviUrl = konfiguracijskiUrl.split("/api");
		return noviUrl[0];
	}
}
