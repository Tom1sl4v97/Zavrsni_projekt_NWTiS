package org.foi.nwtis.ttomiek.aplikacija_5.dretve;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.foi.nwtis.podaci.Aerodrom;
import org.foi.nwtis.ttomiek.aplikacija_5.slusaci.SlusacAplikacije;
import org.foi.nwtis.ttomiek.aplikacija_5.wsock.Info;
import org.foi.nwtis.ttomiek.vjezba_06.konfiguracije.bazaPodataka.PostavkeBazaPodataka;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import jakarta.annotation.Resource;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;
import jakarta.xml.ws.WebServiceContext;

/**
 * Klasa Osvjezivac.
 */
public class Osvjezivac extends Thread {

	/** Parametar za vrijeme spavanja. */
	private int vrijemeSpavanja = 0;

	/** Parametar boolean kraj. */
	private boolean kraj = false;

	/** Parametar poruke servera. */
	public static String porukaServera;

	/** Parametar web service konteksta. */
	@Resource
	private WebServiceContext wsContext;

	/** Parametar za postavke baze podataka. */
	private PostavkeBazaPodataka pbp;

	/**
	 * Konstruktor klase.
	 *
	 * @param vrijemeSpavanja parametar za vrijeme spavanja dretve
	 */
	public Osvjezivac() {
		this.pbp = SlusacAplikacije.dohvatiPBP();
		this.vrijemeSpavanja = Integer.parseInt(pbp.dajPostavku("ciklus.spavanje"));
	}

	/**
	 * Metoda za pokretanje dretve.
	 */
	@Override
	public synchronized void start() {
		super.start();
	}

	/**
	 * Metoda koja se vrši kod pokretanja dretve.
	 */
	@Override
	public void run() {
		while (!kraj) {
			String pattern = "dd.MM.yyyy. HH:mm:ss";
			SimpleDateFormat formatiraniDatum = new SimpleDateFormat(pattern);
			String datum = formatiraniDatum.format(new Date());
			int brojAerodroma = dohvatiBrojPratecihAerodroma();
			String poruka = "Trenutni datum: " + datum + ", ukupno aerodroma: " + brojAerodroma;
			Info.informiraj(poruka);
			try {
				Osvjezivac.porukaServera = poruka;
				sleep(vrijemeSpavanja);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Metoda najprije dohvaća token korisnika iz postavki, zatim se dohvaća lista
	 * svih aerodroma kako bi dobili trenutni broj pratiećih aerodroma.
	 *
	 * @return vraća int vrijednost broja trenutnih pratećih aerodroma
	 */
	private int dohvatiBrojPratecihAerodroma() {
		String korIme = this.pbp.dajPostavku("sustav.korisnik");
		String lozinka = this.pbp.dajPostavku("sustav.lozinka");

		String konfiguracijskiUrl = this.pbp.dajPostavku("adresa.app_3");
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

		Response restOdgovorAerodromi = null;
		if (idTokena > 0) {
			String urlAerodroma = odvojiUrl(konfiguracijskiUrl) + "-aplikacija_3/api/aerodromi";
			WebTarget webResourceAerodromi = client.target(urlAerodroma).queryParam("vrsta", 1);
			restOdgovorAerodromi = webResourceAerodromi.request().header("korisnik", korIme).header("zeton", idTokena)
					.get();
		}

		List<Aerodrom> listaPratecihAerodroma = null;
		if (restOdgovorAerodromi.getStatus() == 200) {
			String odgovor = restOdgovorAerodromi.readEntity(String.class);
			Gson gson = new Gson();
			listaPratecihAerodroma = new ArrayList<Aerodrom>();
			listaPratecihAerodroma.addAll(Arrays.asList(gson.fromJson(odgovor, Aerodrom[].class)));
		}
		return listaPratecihAerodroma.size();
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
	 * Metoda za prekiranje rad dretve.
	 */
	@Override
	public void interrupt() {
		kraj = true;
		super.interrupt();
	}

}
