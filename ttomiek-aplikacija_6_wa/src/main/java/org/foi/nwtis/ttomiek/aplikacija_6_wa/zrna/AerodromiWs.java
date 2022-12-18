package org.foi.nwtis.ttomiek.aplikacija_6_wa.zrna;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.foi.nwtis.podaci.Aerodrom;
import org.foi.nwtis.ttomiek.ws.aerodromi.Aerodromi;
import org.foi.nwtis.ttomiek.ws.aerodromi.WsAerodromi;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.xml.ws.WebServiceRef;

/**
 * Klasa AerodromiWs.
 */
@RequestScoped
@Named("aerodromiWs")
public class AerodromiWs {

	/** Parametar web servis. */
	@WebServiceRef(wsdlLocation = "http://localhost:9090/ttomiek-aplikacija_5/aerodromi?wsdl")
	private Aerodromi service;

	/** String vrijednost icao koda za unos podataka. */
	private String icao;

	/** String vrijednost vremena od za unos podataka. */
	private String vrijemeOd;

	/** String vrijednost vremena do za unos podataka. */
	private String vrijemeDo;

	/** String vrijednost icao koda polazišta za unos podataka. */
	private String icaoPolazista;

	/** String vrijednost icao koda dolazišta za unos podataka. */
	private String icaoDolazista;

	/** Int vrijednost udaljenosti za ispis podataka. */
	private int udaljenost;

	/** Lista svih aerodroma, koristi se za prikaz svih / pratećih aerodroma */
	private List<Aerodrom> aerodromi = new ArrayList<Aerodrom>();

	/** Lista svih aerodroma polaska. koristi se za prikaz podataka */
	private List<org.foi.nwtis.ttomiek.ws.aerodromi.AvionLeti> listaPolaska = new ArrayList<org.foi.nwtis.ttomiek.ws.aerodromi.AvionLeti>();

	/**
	 * Metoda dohvaća listu svih aerodrome, koristi se za pogled 6.4.
	 *
	 * @return listu svih aerodorma
	 */
	public List<Aerodrom> getDajSveAerodrome() {
		return aerodromi;
	}

	/**
	 * Metoda postavlja listu svih aerodroma.
	 *
	 * @param aerodromi lista svih aerodroma
	 */
	public void setDajAerodrome(List<Aerodrom> aerodromi) {
		this.aerodromi = aerodromi;
	}

	/**
	 * Dohvaćanje liste aerodroma polaska, koristi se kod pogleda 6.6.
	 *
	 * @return vrača listu pratećih aerodroma
	 */
	public List<org.foi.nwtis.ttomiek.ws.aerodromi.AvionLeti> getDajPolaske() {
		return this.listaPolaska;
	}

	/**
	 * Postavljanje liste aerodroma polaska.
	 *
	 * @param listaPolaska lista aerodroma polaska
	 */
	public void setDajPolaske(List<org.foi.nwtis.ttomiek.ws.aerodromi.AvionLeti> listaPolaska) {
		this.listaPolaska = listaPolaska;
	}

	/**
	 * Dohvaćanje svih pratećih aerodroma
	 *
	 * @return vraća listu pratećih aerodroma
	 */
	public List<Aerodrom> getDajAerodromePreuzimanja() {
		dohvatiAerodrome(true);
		return this.aerodromi;
	}

	/**
	 * Dohvaćanje icao koda.
	 *
	 * @return vraća string vrijednost icao koda aerodroma
	 */
	public String getIcao() {
		return this.icao;
	}

	/**
	 * Postavljanje icao koda.
	 *
	 * @param icao string vrijednost icao koda aerodroma
	 */
	public void setIcao(String icao) {
		this.icao = icao;
	}

	/**
	 * Dohvaća vrijeme od.
	 *
	 * @return string vrijednost vremena od
	 */
	public String getVrijemeOd() {
		return this.vrijemeOd;
	}

	/**
	 * Postavlja vrijeme od.
	 *
	 * @param vrijemeOd string vrijednost vremena od
	 */
	public void setVrijemeOd(String vrijemeOd) {
		this.vrijemeOd = vrijemeOd;
	}

	/**
	 * Dohvaća vrijeme do.
	 *
	 * @return vraća string vrijednost vremena do
	 */
	public String getVrijemeDo() {
		return this.vrijemeDo;
	}

	/**
	 * Postavlja vrijeme do.
	 *
	 * @param vrijemeDo string vrijednost vremena do
	 */
	public void setVrijemeDo(String vrijemeDo) {
		this.vrijemeDo = vrijemeDo;
	}

	/**
	 * Dohvaća icao polazišta.
	 *
	 * @return the vraća string vrijednost icao koda polazišta
	 */
	public String getIcaoPolazista() {
		return this.icaoPolazista;
	}

	/**
	 * Postavlja icao polazište.
	 *
	 * @param icaoPolazista string vrijednost icao koda polazišta
	 */
	public void setIcaoPolazista(String icaoPolazista) {
		this.icaoPolazista = icaoPolazista;
	}

	/**
	 * Dohvaća icao dolazišta.
	 *
	 * @return vraća string vrijednost icao koda dolazišta
	 */
	public String getIcaoDolazista() {
		return this.icaoDolazista;
	}

	/**
	 * Postavlja icao dolazište.
	 *
	 * @param icaoDolazista string vrijednost icao koda dolazišta
	 */
	public void setIcaoDolazista(String icaoDolazista) {
		this.icaoDolazista = icaoDolazista;
	}

	/**
	 * Dohvaća udaljenost.
	 *
	 * @return vraća string vrijednost udaljenosti
	 */
	public int getUdaljenost() {
		return this.udaljenost;
	}

	/**
	 * Postavlja udaljenost.
	 *
	 * @param udaljenost string vrijednost udaljenosti
	 */
	public void setUdaljenost(int udaljenost) {
		this.udaljenost = udaljenost;
	}

	/**
	 * Metoda dohvaća sve aerodrome, koristi se kod pogleda 6.4.
	 */
	public void dohvatiSveAerodrome() {
		dohvatiAerodrome(false);
	}

	/**
	 * Metoda dohvaća sve prateće aerodrome, koristi se kod pogleda 6.4.
	 */
	public void dohvatiPraceneAerodrome() {
		dohvatiAerodrome(true);
	}

	/**
	 * Metoda dohvaća sve aerodrome polaska u formatu datuma dd.MM.yyyy, koristi se
	 * kod pogleda 6.6.
	 */
	public void dohvatiPolaskeDan() {
		dohvatiPolaske(true);
	}

	/**
	 * Metoda dohvaća sve aerodrome polaska u formatu datuma u sekundama / epoch,
	 * koristi se kod pogleda 6.6.
	 */
	public void dohvatiPolaskeVrijeme() {
		dohvatiPolaske(false);
	}

	/**
	 * Metoda dohvaća udaljenost između dva icao koda, koristi se kod pogleda 6.5.
	 */
	public void dohvatiUdaljenost() {
		int idTokena = dohvatiToken();

		if (idTokena >= 0) {
			String urlAerodromi = "http://localhost:8080/ttomiek-aplikacija_3/api/aerodromi";
			Client client = ClientBuilder.newClient();
			WebTarget webResourceAerodromi = client.target(urlAerodromi).path("/" + icaoPolazista)
					.path("/" + icaoDolazista);

			Response restOdgovor = webResourceAerodromi.request().header("korisnik", "ttomiek")
					.header("zeton", idTokena).get();

			if (restOdgovor.getStatus() == 200) {
				String odgovor = restOdgovor.readEntity(String.class);
				JsonObject jobj = new Gson().fromJson(odgovor, JsonObject.class);
				int udaljenost = jobj.get("udaljenost").getAsInt();
				this.udaljenost = udaljenost;
			}
		}
	}

	/**
	 * Metoda dohvaća listu aerodroma polaska sa applikacije_5, koristi se kod
	 * pogleda 6.6.
	 *
	 * @param odabir boolean vrijednost odabira, koristi se kako bi se znalo koji
	 *               format datuma treba koristiti
	 */
	private void dohvatiPolaske(boolean odabir) {
		if (service == null)
			service = new Aerodromi();
		int idTokena = dohvatiToken();

		WsAerodromi wsAerdoromi = service.getWsAerodromiPort();
		if (odabir) {
			this.listaPolaska = wsAerdoromi.dajPolaskeDan("ttomiek", String.valueOf(idTokena), this.icao,
					this.vrijemeOd, this.vrijemeDo);
		} else {
			this.listaPolaska = wsAerdoromi.dajPolaskeVrijeme("ttomiek", String.valueOf(idTokena), this.icao,
					this.vrijemeOd, this.vrijemeDo);
		}
	}

	/**
	 * Metoda dohvaća listu aerodroma od aplikacije_3, koristi se kod pogleda 6.4.
	 *
	 * @param odabir boolean vrijednost odabira, koristi se kako bi se znalo koje
	 *               aerodrome treba uzeti (prateće ili sve)
	 */
	private void dohvatiAerodrome(boolean odabir) {
		int idTokena = dohvatiToken();

		if (idTokena >= 0) {
			String urlAerodromi = "http://localhost:8080/ttomiek-aplikacija_3/api/aerodromi";
			Client client = ClientBuilder.newClient();
			WebTarget webResourceAerodromi = client.target(urlAerodromi);
			if (odabir) {
				urlAerodromi += "?vrsta=1";
				webResourceAerodromi = client.target(urlAerodromi);
			}
			Response restOdgovor = webResourceAerodromi.request().header("korisnik", "ttomiek")
					.header("zeton", idTokena).get();

			List<Aerodrom> listaAerodroma = null;
			if (restOdgovor.getStatus() == 200) {
				String odgovor = restOdgovor.readEntity(String.class);
				Gson gson = new Gson();
				listaAerodroma = new ArrayList<Aerodrom>();
				listaAerodroma.addAll(Arrays.asList(gson.fromJson(odgovor, Aerodrom[].class)));
			}

			this.aerodromi = listaAerodroma;
		}
	}

	/**
	 * Metoda najprije dohvaća ID tokena korisnika, zatim dohvaća podatke traženog
	 * aerodroma i potom ga zapisuje u bazu preko aplikacije_3, koristi se kod
	 * pogleda 6.4 .
	 */
	public void dodajAerodrom() {
		int idTokena = dohvatiToken();

		if (idTokena >= 0) {
			String urlAerodromi = "http://localhost:8080/ttomiek-aplikacija_3/api/aerodromi/" + this.icao;
			Client client = ClientBuilder.newClient();
			WebTarget webResourceAerodromi = client.target(urlAerodromi);

			Response restOdgovorAerodromi = webResourceAerodromi.request().header("korisnik", "ttomiek")
					.header("zeton", idTokena).get();

			if (restOdgovorAerodromi.getStatus() == 200) {
				String odgovor = restOdgovorAerodromi.readEntity(String.class);

				urlAerodromi = "http://localhost:8080/ttomiek-aplikacija_3/api/aerodromi/";
				webResourceAerodromi = client.target(urlAerodromi);
				webResourceAerodromi.request().header("korisnik", "ttomiek").header("zeton", idTokena)
						.post(Entity.entity(odgovor, MediaType.APPLICATION_JSON), Response.class);
				dohvatiAerodrome(true);
			}
		}
	}

	/**
	 * Metoda dohvaća ID tokena korisnika od aplikacije_3, koristi se kod pogleda
	 * 6.4 i 6.5.
	 *
	 * @return vraća int vrijednost dobivenog ID-a tokena korisnika
	 */
	private int dohvatiToken() {
		String urlProvjere = "http://localhost:8080/ttomiek-aplikacija_3/api/provjere";

		Client client = ClientBuilder.newClient();
		WebTarget webResourceProvjere = client.target(urlProvjere);

		Response restOdgovorProvjere = webResourceProvjere.request().header("korisnik", "ttomiek")
				.header("lozinka", "123456").get();
		int idTokena = 0;
		if (restOdgovorProvjere.getStatus() == 200) {
			String odgovor = restOdgovorProvjere.readEntity(String.class);
			JsonObject jobj = new Gson().fromJson(odgovor, JsonObject.class);
			idTokena = jobj.get("zeton").getAsInt();
		}

		return idTokena;
	}
}
