package org.foi.nwtis.ttomiek.aplikacija_1;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.foi.nwtis.podaci.Aerodrom;

import com.google.gson.Gson;

/**
 * Klasa dretve zahtjeva.
 */
public class DretvaZahtjeva extends Thread {

	/** Socket veze koja se dobiva. */
	Socket veza = null;

	/**
	 * Vrijednost servera udaljenosti koja se dobiva, kako bi se moglo pristupiti
	 * metodama servera udaljenosti.
	 */
	ServerUdaljenosti serverUdalj;

	/**
	 * Boolean vrijednost koja se koristi kod gašenja servera, kako bi se dretva
	 * ispravno zatvorila kada završi sa poslom.
	 */
	private boolean radServera = false;

	/**
	 * Konstruktor klase dretve zahtjeva.
	 *
	 * @param veza        parametar veze
	 * @param serverUdalj parametar servera udaljenosti
	 */
	public DretvaZahtjeva(Socket veza, ServerUdaljenosti serverUdalj) {
		super();
		this.veza = veza;
		this.serverUdalj = serverUdalj;

	}

	/**
	 * Metoda koja se vrši na početku kako bi počeo rad dretve.
	 */
	@Override
	public synchronized void start() {
		super.start();
	}

	/**
	 * Metoda koja se vrši prva nakon starta, metoda čita zahtjev korisnika i
	 * obrađuje ga.
	 */
	@Override
	public void run() {
		try (InputStreamReader isr = new InputStreamReader(this.veza.getInputStream(), Charset.forName("UTF-8"));
				OutputStreamWriter osw = new OutputStreamWriter(this.veza.getOutputStream(),
						Charset.forName("UTF-8"));) {

			StringBuilder zahtjev = new StringBuilder();
			while (true) {
				int i = isr.read();
				if (i == -1) {
					break;
				}
				zahtjev.append((char) i);
			}

			System.out.println("ZAHTJEV: " + zahtjev.toString());

			String odgovor = obradiNaredbu(zahtjev.toString());

			osw.write(odgovor);
			osw.flush();

			this.veza.shutdownOutput();

			this.interrupt();
		} catch (IOException e) {
			System.out.println("ERROR 09 Dogodila se je pogreška prilikom čitanja podataka");
		}
	}

	/**
	 * Metoda uspoređuje zahtjev korisnika i na temelju zahtjeva, obrađuje se
	 * dobiveni zahtjev.
	 *
	 * @param zahtjev string vrijednost zahtjeva
	 * @return vraća string vrijednost odgovora ili greške
	 */
	private String obradiNaredbu(String zahtjev) {
		String odgovor = "ERROR 14 Krivi format zahtjeva!";

		Pattern pStatus = Pattern.compile("^STATUS$");
		Pattern pQuit = Pattern.compile("^QUIT$");
		Pattern pInit = Pattern.compile("^INIT$");
		Pattern pLoad = Pattern.compile("^LOAD .*$");
		Pattern pAirportUdaljenost = Pattern.compile("^DISTANCE ([A-Z]{4}) ([A-Z]{4})$");
		Pattern pClear = Pattern.compile("^CLEAR$");

		Matcher mStatus = pStatus.matcher(zahtjev);
		Matcher mQuit = pQuit.matcher(zahtjev);
		Matcher mInit = pInit.matcher(zahtjev);
		Matcher mLoad = pLoad.matcher(zahtjev);
		Matcher mAirportUdaljenost = pAirportUdaljenost.matcher(zahtjev);
		Matcher mClear = pClear.matcher(zahtjev);

		if (mStatus.matches()) {
			odgovor = dohvatiStatus();
		} else if (mQuit.matches()) {
			odgovor = izvrsiQuit();
		} else if (mInit.matches()) {
			odgovor = inicijaliziraj();
		} else if (mLoad.matches()) {
			odgovor = ucitajPodatke(zahtjev);
		} else if (mAirportUdaljenost.matches()) {
			odgovor = dohvatiUdaljenost(zahtjev);
		} else if (mClear.matches()) {
			odgovor = izvrsiClear();
		}

		return odgovor;
	}

	/**
	 * Metoda prazni listu aerodroma kod servera udaljenosti.
	 *
	 * @return vraća string vrijednost odgovora ili greške
	 */
	private String izvrsiClear() {
		String odgovor = provjeriStatusServeraHibridni("CLEAR");
		if (odgovor != null)
			return odgovor;

		odgovor = provjeriStatusServeraInicijalizacije("CLEAR");
		if (odgovor != null)
			return odgovor;

		odgovor = "OK";
		this.serverUdalj.ocistiListuAerodroma();
		this.serverUdalj.postaviStatusServera(0);

		return odgovor;
	}

	/**
	 * Metoda provjerava status servera za hibridnu vrstu servera.
	 *
	 * @param zahtjev string vrijednost zahtjeva
	 * @return vraća null ako je zahtjev ispravan, inače string grešku
	 */
	private String provjeriStatusServeraHibridni(String zahtjev) {
		int statusServera = this.serverUdalj.dohvatiStatusServera();

		if (statusServera == 0) {
			return "ERROR 01 Poslužitelj trenutno ne može izvršiti radnju " + zahtjev
					+ ", jer se nalazi u stanju hibernacije!";
		}

		return null;
	}

	/**
	 * Metoda provjerava status servera za inicijalizacijsku vrstu servera.
	 *
	 * @param zahtjev string vrijednost zahtjeva
	 * @return vraća null ako je zahtjev ispravan, inače string grešku
	 */
	private String provjeriStatusServeraInicijalizacije(String zahtjev) {
		int statusServera = this.serverUdalj.dohvatiStatusServera();

		if (statusServera == 1) {
			return "ERROR 02 Poslužitelj trenutno ne može izvršiti radnju " + zahtjev
					+ ", jer se nalazi u stanju inicijalizacije!";
		}

		return null;
	}

	/**
	 * Metoda provjerava status servera za aktivnu vrstu servera.
	 *
	 * @param zahtjev string vrijednost zahtjeva
	 * @return vraća null ako je zahtjev ispravan, inače string grešku
	 */
	private String provjeriStatusServeraAktivan(String zahtjev) {
		int statusServera = this.serverUdalj.dohvatiStatusServera();

		if (statusServera == 2) {
			return "ERROR 03 Poslužitelj trenutno ne može izvršiti radnju " + zahtjev
					+ ", jer se nalazi u stanju aktivnosti!";
		}

		return null;
	}

	/**
	 * Metoda provjerava status servera za udaljenost, te dobiva udaljenost između
	 * dva aerodroma.
	 *
	 * @param zahtjev string vrijednost zahtjeva
	 * @return vraća odgovor za zahtjev distance ili string grešku
	 */
	private String dohvatiUdaljenost(String zahtjev) {
		String odgovor = provjeriStatusServeraHibridni("DISTANCE");
		if (odgovor != null)
			return odgovor;

		odgovor = provjeriStatusServeraInicijalizacije("DISTANCE");
		if (odgovor != null)
			return odgovor;

		odgovor = izračunajUdaljenost(zahtjev);

		return odgovor;
	}

	/**
	 * Metoda dohvaća podatke o treženim aerodromima i dohvaća njihovu udaljenost
	 *
	 * @param zahtjev string vrijednost zahtjeva
	 * @return vraća string odgovora, sa izračunatom udaljenosti izraženo u km ili
	 *         string vrijednost greške
	 */
	private String izračunajUdaljenost(String zahtjev) {
		String podaci[] = zahtjev.split(" ");

		String aerodromPolazista = podaci[1];
		String aerodromDolazista = podaci[2];

		Aerodrom podaciAerPolazista = saznajPodatkeAer(aerodromPolazista);
		Aerodrom podaciAerDolazista = saznajPodatkeAer(aerodromDolazista);

		if (podaciAerDolazista == null && podaciAerPolazista == null)
			return "ERROR 13 Ne postoji ni POLAZNI, ni DOLAZNI navedeni aerodrom sa nazivima: " + podaci[1] + " i "
					+ podaci[2];
		else if (podaciAerPolazista == null)
			return "ERROR 11 Ne postoji POLAZNI aerodrom sa nazivom: " + podaci[1];
		else if (podaciAerDolazista == null)
			return "ERROR 12 Ne postoji DOLAZNI aerodrom sa nazivom: " + podaci[2];
		else
			return "OK " + izracunUdaljenosti(Double.parseDouble(podaciAerPolazista.getLokacija().getLongitude()),
					Double.parseDouble(podaciAerDolazista.getLokacija().getLongitude()),
					Double.parseDouble(podaciAerPolazista.getLokacija().getLatitude()),
					Double.parseDouble(podaciAerDolazista.getLokacija().getLatitude()));
	}

	/**
	 * Metoda pronalazi podatke traženog aerodroma.
	 *
	 * @param aerodrom string vrijednost icao koda od aerodroma
	 * @return vraća aerodrom vrijednost o pronađenom aerodromu, inače null
	 */
	private Aerodrom saznajPodatkeAer(String aerodrom) {
		List<Aerodrom> listaAerodroma = this.serverUdalj.dohvatiListuAerodroma();

		if (listaAerodroma == null)
			return null;

		for (Aerodrom a : listaAerodroma) {
			if (a.getIcao().equals(aerodrom))
				return a;
		}

		return null;
	}

	/**
	 * Metoda računa udaljenost između dvije točke na zamlji, autor programskog
	 * koda: Twinkl Bajaj, dostupno:
	 * https://www.geeksforgeeks.org/program-distance-two-points-earth/
	 *
	 * @param lat1 koordinatna točka o geo. širini prvog aerodroma
	 * @param lat2 koordinatna točka o geo. širini drugog aerodroma
	 * @param lon1 koordinatna točka o geo. dužini prvog aerodroma
	 * @param lon2 koordinatna točka o geo. dužini drugog aerodroma
	 * @return vraća string udaljenosti između dva aerodroma
	 */
	public String izracunUdaljenosti(double lat1, double lat2, double lon1, double lon2) {

		lon1 = Math.toRadians(lon1);
		lon2 = Math.toRadians(lon2);
		lat1 = Math.toRadians(lat1);
		lat2 = Math.toRadians(lat2);

		double dlon = lon2 - lon1;
		double dlat = lat2 - lat1;
		double a = Math.pow(Math.sin(dlat / 2), 2) + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(dlon / 2), 2);

		double c = 2 * Math.asin(Math.sqrt(a));
		double r = 6371;

		int rezultat = (int) (c * r + 0.5);

		return Integer.toString(rezultat);
	}

	/**
	 * Metoda provjerava da li se zahtjev prvo može izvesti, te ako se može provodi
	 * se metoda za zapis aerodroma u kolekciju.
	 *
	 * @param zahtjev string vrijednost zahtjeva
	 * @return vraća string vrijednost odgovora ili greške
	 */
	private String ucitajPodatke(String zahtjev) {
		String odgovor = provjeriStatusServeraHibridni("LOAD");
		if (odgovor != null)
			return odgovor;

		odgovor = provjeriStatusServeraAktivan("LOAD");
		if (odgovor != null)
			return odgovor;

		int kolicinaZapisa = zapisiPodatke(zahtjev);
		this.serverUdalj.postaviStatusServera(2);

		return "OK " + kolicinaZapisa;
	}

	/**
	 * Metoda pretvara json string u kolekciju aerodroma, te se vrši zapis liste
	 * aerodroma i postavlja status servera na 2.
	 *
	 * @param zahtjev string vrijednost zahtjeva
	 * @return vraća broj zapisanih podataka iz liste
	 */
	private int zapisiPodatke(String zahtjev) {
		String[] json = zahtjev.split("LOAD ");
		String jsonString = json[1];

		Gson gson = new Gson();
		Aerodrom[] listaAerodroma = gson.fromJson(jsonString, Aerodrom[].class);

		this.serverUdalj.postaviListuAerodroma(Arrays.asList(listaAerodroma));
		return listaAerodroma.length;
	}

	/**
	 * Metoda inicijalizira server udaljenosti i postavlja status servera na 1.
	 *
	 * @return vraća string odgovora ili greške
	 */
	private String inicijaliziraj() {
		String odgovor = provjeriStatusServeraInicijalizacije("INIT");
		if (odgovor != null)
			return odgovor;

		odgovor = provjeriStatusServeraAktivan("INIT");
		if (odgovor != null)
			return odgovor;

		this.serverUdalj.postaviStatusServera(1);

		return "OK";
	}

	/**
	 * Metoda postavlja boolean vrijednost na true za gašenje servera.
	 *
	 * @return vraća string vrijednost odgovora
	 */
	private String izvrsiQuit() {
		this.radServera = true;
		return "OK";
	}

	/**
	 * MEtoda dohvaća trenutni status servera.
	 *
	 * @return string vrijednost odgovora
	 */
	private String dohvatiStatus() {
		return "OK " + this.serverUdalj.dohvatiStatusServera();
	}

	/**
	 * Metoda vrši prekidanje rada dretve i smanjuje brojač dretvi na serveru
	 * udaljenosti.
	 */
	@Override
	public void interrupt() {
		if (this.radServera)
			this.serverUdalj.prekiniRadServera();
		this.serverUdalj.smanjiBrojac();
		super.interrupt();
	}
}
