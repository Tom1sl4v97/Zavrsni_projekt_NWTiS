package org.foi.nwtis.ttomiek.app_4.mvc;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.foi.nwtis.podaci.Aerodrom;
import org.foi.nwtis.podaci.Korisnik;
import org.foi.nwtis.ttomiek.app_4.podaci.Grupa;
import org.foi.nwtis.ttomiek.app_4.podaci.TokenKorisnika;
import org.foi.nwtis.ttomiek.vjezba_06.konfiguracije.bazaPodataka.PostavkeBazaPodataka;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Klasa korisnici klijent koji sadrži potrebne metode za komunikaciju sa app_3.
 */
public class KorisniciKlijent {

	/**
	 * Metoda odvaja konfiguracijski url (iz postavki) kako bi se pripasao potrebno
	 * url-u.
	 *
	 * @param konfiguracijskiUrl string vrijednost konfiguracijskog url-a
	 * @return string virjednost potrebnog url-a
	 */
	private String odvojiUrl(String konfiguracijskiUrl) {
		String[] noviUrl = konfiguracijskiUrl.split("/api");
		return noviUrl[0];
	}

	/**
	 * Metoda najprije dohvaća ispravni token korisnika iz postavki i potom
	 * registrira novog korisnika.
	 *
	 * @param noviKorisnik podaci novog korisnika
	 * @param pbp          parametar za postavke baze podataka
	 * @return vraća true ako je uspiješno dodao novog korisnika u bazu podataka,
	 *         inače false
	 */
	public boolean registrirajKorisnika(Korisnik noviKorisnik, PostavkeBazaPodataka pbp) {
		String konfiguracijskiUrl = pbp.dajPostavku("adresa.app_3");
		String urlKorinika = odvojiUrl(konfiguracijskiUrl);
		String urlProvjere = urlKorinika + "-aplikacija_3/api/provjere";
		urlKorinika += "-aplikacija_3/api/korisnici";

		String korImeAutent = pbp.dajPostavku("sustav.korisnik");
		String lozinkaAutent = pbp.dajPostavku("sustav.lozinka");

		Client client = ClientBuilder.newClient();
		WebTarget webResourceKorisnika = client.target(urlKorinika);
		WebTarget webResourceProvjere = client.target(urlProvjere);

		Response restOdgovorProvjere = webResourceProvjere.request().header("korisnik", korImeAutent)
				.header("lozinka", lozinkaAutent).get();

		int idTokena = dohvatiToken(restOdgovorProvjere);

		if (idTokena >= 0) {
			Gson gson = new Gson();
			String json = gson.toJson(noviKorisnik);
			Response restOdgovorKorisnici = webResourceKorisnika.request().header("korisnik", korImeAutent)
					.header("zeton", idTokena).post(Entity.entity(json, MediaType.APPLICATION_JSON), Response.class);
			if (restOdgovorKorisnici.getStatus() == 200) {
				return true;
			}
		}
		return false;

	}

	/**
	 * Metoda tretvara dobiveni rest odgovor od servera app_3 u objekt token
	 * korisnika.
	 *
	 * @param restOdgovor parametar rest odgovora
	 * @return vraća dobiveni ID tokena ili -1 ako ne postoji token
	 */
	private int dohvatiToken(Response restOdgovor) {
		if (restOdgovor.getStatus() == 200) {
			String odgovor = restOdgovor.readEntity(String.class);
			JsonObject jobj = new Gson().fromJson(odgovor, JsonObject.class);
			int id = jobj.get("zeton").getAsInt();
			return id;
		}

		return -1;
	}

	/**
	 * Metoda dohvaća ispravni token korisnika.
	 *
	 * @param korIme  string vrijednost korisničkog imena
	 * @param lozinka string vrijednost lozinke
	 * @param pbp     parametar za postavke baze podataka
	 * @return vraća dobiveni token ili null ako ne postoji token
	 */
	public TokenKorisnika prijavaKorisnika(String korIme, String lozinka, PostavkeBazaPodataka pbp) {
		String konfiguracijskiUrl = pbp.dajPostavku("adresa.app_3");
		String urlProvjere = odvojiUrl(konfiguracijskiUrl) + "-aplikacija_3/api/provjere";

		Client client = ClientBuilder.newClient();
		WebTarget webResourceProvjere = client.target(urlProvjere);

		Response restOdgovorProvjere = webResourceProvjere.request().header("korisnik", korIme)
				.header("lozinka", lozinka).get();

		if (restOdgovorProvjere.getStatus() == 200) {
			String odgovor = restOdgovorProvjere.readEntity(String.class);
			JsonObject json = new Gson().fromJson(odgovor, JsonObject.class);
			int id = json.get("zeton").getAsInt();
			long vrijediDo = json.get("vrijeme").getAsLong();

			TokenKorisnika token = new TokenKorisnika(id, korIme, 1, vrijediDo);

			return token;
		}

		return null;
	}

	/**
	 * Metoda dohvaća sve korisnike.
	 *
	 * @param tokenKorisnika podaci tokena korisnika
	 * @param pbp            parametar za postavke baze podataka
	 * @return vraća listu svih korisnika
	 */
	public List<Korisnik> dohvatiKorisnike(TokenKorisnika tokenKorisnika, PostavkeBazaPodataka pbp) {
		String konfiguracijskiUrl = pbp.dajPostavku("adresa.app_3");
		String urlKorisnici = odvojiUrl(konfiguracijskiUrl) + "-aplikacija_3/api/korisnici";

		Client client = ClientBuilder.newClient();
		WebTarget webResourceProvjere = client.target(urlKorisnici);

		Response restOdgovor = webResourceProvjere.request().header("korisnik", tokenKorisnika.getKorisnik())
				.header("zeton", tokenKorisnika.getId()).get();
		List<Korisnik> listaKorisnika = null;
		if (restOdgovor.getStatus() == 200) {
			String odgovor = restOdgovor.readEntity(String.class);
			Gson gson = new Gson();
			listaKorisnika = new ArrayList<Korisnik>();
			listaKorisnika.addAll(Arrays.asList(gson.fromJson(odgovor, Korisnik[].class)));
		}

		return listaKorisnika;
	}

	/**
	 * Metoda provjerava privilegije korisnika.
	 *
	 * @param tokenKorisnika podaci tokena korisnika
	 * @param pbp            parametar za postavke baze podataka
	 * @return vraća true ako traženi korisnik posijeduje privilegije admina, inače
	 *         false
	 */
	public boolean provjeriPrivilegijeAdmina(TokenKorisnika tokenKorisnika, PostavkeBazaPodataka pbp) {
		String konfiguracijskiUrl = pbp.dajPostavku("adresa.app_3");
		String urlKorisniciGrupe = odvojiUrl(konfiguracijskiUrl) + "-aplikacija_3/api/korisnici/"
				+ tokenKorisnika.getKorisnik() + "/grupe";

		Client client = ClientBuilder.newClient();
		WebTarget webResource = client.target(urlKorisniciGrupe);

		Response restOdgovor = webResource.request().header("korisnik", tokenKorisnika.getKorisnik())
				.header("zeton", tokenKorisnika.getId()).get();
		List<Grupa> listaGrupe = null;

		if (restOdgovor.getStatus() == 200) {
			String odgovor = restOdgovor.readEntity(String.class);
			Gson gson = new Gson();
			listaGrupe = new ArrayList<Grupa>();
			listaGrupe.addAll(Arrays.asList(gson.fromJson(odgovor, Grupa[].class)));
		}
		if (listaGrupe != null) {
			for (Grupa g : listaGrupe) {
				if (g.getGrupa().equals("admin")) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Metoda briđe vlastiti token korisnika.
	 *
	 * @param tokenKorisnika podaci tokena korisnika
	 * @param pbp            parametar za postavke baze podataka
	 * @return vraća null ako je uspiješno obrisan token korisnika, inače status
	 *         odgovora poslužitelja app_3
	 */
	public String obrisiVlastitiToken(TokenKorisnika tokenKorisnika, PostavkeBazaPodataka pbp) {
		String konfiguracijskiUrl = pbp.dajPostavku("adresa.app_3");
		String urlProvjere = odvojiUrl(konfiguracijskiUrl) + "-aplikacija_3/api/provjere/" + tokenKorisnika.getId();

		Client client = ClientBuilder.newClient();
		WebTarget webResourceProvjere = client.target(urlProvjere);

		Korisnik prijavljeniKorisnik = dohvatiPodatkeKorisnika(tokenKorisnika, pbp);

		Response restOdgovor = webResourceProvjere.request().header("korisnik", prijavljeniKorisnik.getKorIme())
				.header("lozinka", prijavljeniKorisnik.getLozinka()).delete();
		if (restOdgovor.getStatus() == 200) {
			return null;
		}
		return restOdgovor.readEntity(String.class);
	}

	/**
	 * Metoda dohvaća podatke korisnika kako bi dobio potrebnu lozinku korisnika za
	 * daljnji rad.
	 *
	 * @param tokenKorisnika podaci tokena korisnika
	 * @param pbp            parametar za postavke baze podataka
	 * @return vraća dobivene podatke traženog korisnika
	 */
	private Korisnik dohvatiPodatkeKorisnika(TokenKorisnika tokenKorisnika, PostavkeBazaPodataka pbp) {
		String konfiguracijskiUrl = pbp.dajPostavku("adresa.app_3");
		String urlKorisnici = odvojiUrl(konfiguracijskiUrl) + "-aplikacija_3/api/korisnici/"
				+ tokenKorisnika.getKorisnik();

		Client client = ClientBuilder.newClient();
		WebTarget webResource = client.target(urlKorisnici);

		Response restOdgovor = webResource.request().header("korisnik", tokenKorisnika.getKorisnik())
				.header("zeton", tokenKorisnika.getId()).get();

		Korisnik podaciKorisnika = null;
		if (restOdgovor.getStatus() == 200) {
			String odgovor = restOdgovor.readEntity(String.class);
			Gson gson = new Gson();
			podaciKorisnika = gson.fromJson(odgovor, Korisnik.class);
		}

		return podaciKorisnika;
	}

	/**
	 * Metoda briše sve valjanje tokene traženog korisnika.
	 *
	 * @param tokenKorisnika podaci tokena korisnika
	 * @param korime         string vrijednost korisničkog imena za kojeg se brišu
	 *                       valjani tokeni
	 * @param pbp            parametar za postavke baze podataka
	 * @return vraća null ako je uspiješno obrisao tokene korisnika, inače status
	 *         odgovora poslužitelja app_3
	 */
	public String obrisiTokenKorisnika(TokenKorisnika tokenKorisnika, String korime, PostavkeBazaPodataka pbp) {
		String konfiguracijskiUrl = pbp.dajPostavku("adresa.app_3");
		String urlProvjere = odvojiUrl(konfiguracijskiUrl) + "-aplikacija_3/api/provjere/korisnik/" + korime;

		Client client = ClientBuilder.newClient();
		WebTarget webResourceProvjere = client.target(urlProvjere);

		Korisnik prijavljeniKorisnik = dohvatiPodatkeKorisnika(tokenKorisnika, pbp);

		Response restOdgovor = webResourceProvjere.request().header("korisnik", prijavljeniKorisnik.getKorIme())
				.header("lozinka", prijavljeniKorisnik.getLozinka()).delete();
		if (restOdgovor.getStatus() == 200) {
			return null;
		}
		return restOdgovor.readEntity(String.class);
	}

	/**
	 * Metoda vrši komunikaciju sa poslužiteljem app_1.
	 *
	 * @param komanda string vrijednost komande koja se provodi nad poslužiteljem
	 * @param adresa  string vrijednost adrese poslužitelja
	 * @param port    int vrijednost porta poslužitelja
	 * @return vraća dobiveni odgovor od poslužitelja
	 */
	public String dohvatiOdgovorPosluzitelja(String komanda, String adresa, int port) {
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
	 * Metoda dohvaća listu aerodroma koji je potreban kod LOAD komande za
	 * poslužitelja app_1.
	 *
	 * @param tokenKorisnika podaci tokena korisnika
	 * @param pbp            parametar za postavke baze podataka
	 * @return vraća string vrijednost json-a sa svim aerodromima
	 */
	public String dohvatiListuAerodroma(TokenKorisnika tokenKorisnika, PostavkeBazaPodataka pbp) {
		String konfiguracijskiUrl = pbp.dajPostavku("adresa.app_3");
		String urlAerodromi = odvojiUrl(konfiguracijskiUrl) + "-aplikacija_3/api/aerodromi";

		Client client = ClientBuilder.newClient();
		WebTarget webResource = client.target(urlAerodromi);

		Response restOdgovor = webResource.request().header("korisnik", tokenKorisnika.getKorisnik())
				.header("zeton", tokenKorisnika.getId()).get();

		List<Aerodrom> listaAerodroma = null;
		if (restOdgovor.getStatus() == 200) {
			String odgovor = restOdgovor.readEntity(String.class);
			Gson gson = new Gson();
			listaAerodroma = new ArrayList<Aerodrom>();
			listaAerodroma.addAll(Arrays.asList(gson.fromJson(odgovor, Aerodrom[].class)));
		}
		Gson gson = new Gson();
		String json = gson.toJson(listaAerodroma);

		return json;
	}

}
