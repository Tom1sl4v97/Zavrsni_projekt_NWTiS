package org.foi.nwtis.ttomiek.aplikacija_2.dretve;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.foi.nwtis.podaci.Aerodrom;
import org.foi.nwtis.rest.klijenti.NwtisRestIznimka;
import org.foi.nwtis.rest.klijenti.OSKlijent;
import org.foi.nwtis.rest.podaci.AvionLeti;
import org.foi.nwtis.ttomiek.aplikacija_2.podaci.AerodromiDolasciDAO;
import org.foi.nwtis.ttomiek.aplikacija_2.podaci.AerodromiPolasciDAO;
import org.foi.nwtis.ttomiek.aplikacija_2.podaci.AerodromiPraceniDAO;
import org.foi.nwtis.ttomiek.aplikacija_2.podaci.AerodromiProblemi;
import org.foi.nwtis.ttomiek.aplikacija_2.podaci.AerodromiProblemiDAO;
import org.foi.nwtis.ttomiek.vjezba_06.konfiguracije.bazaPodataka.PostavkeBazaPodataka;

/**
 * Klasa dretve preuzimanja rasporeda aerodroma.
 */
public class PreuzimanjeRasporedaAerodroma extends Thread {

	/** Parametar preuzimanje odmak. */
	private long preuzimanjeOdmakTO;

	/** Parametar preuzimanje pauza. */
	private int preuzimanjePauza;

	/** Parametar preuzimanje. */
	private long preuzimanjeOd;

	/** Parametar preuzimanje. */
	private long preuzimanjeDo;

	/** Parametar preuzimanje vrijeme. */
	private int preuzimanjeVrijemeTV;

	/** Parametar ciklus koriekcija. */
	private int ciklusKoriekcijaCK;

	/** Parametar ciklus vrijeme. */
	private long ciklusVrijemeTC;

	/** Parametar korisničko imena. */
	private String korime;

	/** Parametar lozinka. */
	private String lozinka;

	/** Parametar n koji se koristi kod povećavanja virtualnog brojača dretve. */
	private int n = 0;

	/** Parametar os klijenta. */
	private OSKlijent osKlijent;

	/** Parametar PostavkeBazaPodataka. */
	private PostavkeBazaPodataka pbp;

	/** Parametar početaka rada. */
	public String pocetakRada;

	/**
	 * Inicijalizacija klase preuzimanje rasporeda aerodroma.
	 *
	 * @param pbp parametar PostavkeBazaPodataka
	 */
	public PreuzimanjeRasporedaAerodroma(PostavkeBazaPodataka pbp) {
		super();
		this.pbp = pbp;
	}

	/**
	 * Početak rada dretve.
	 */
	@Override
	public synchronized void start() {
		this.preuzimanjeOdmakTO = Integer.parseInt(pbp.dajPostavku("preuzimanje.odmak")) * 24 * 60 * 60 * 1000;
		this.preuzimanjePauza = Integer.parseInt(pbp.dajPostavku("preuzimanje.pauza"));
		this.preuzimanjeOd = pretvoriDatumEpoch(pbp.dajPostavku("preuzimanje.od"));
		this.preuzimanjeDo = pretvoriDatumEpoch(pbp.dajPostavku("preuzimanje.do"));
		this.preuzimanjeVrijemeTV = Integer.parseInt(pbp.dajPostavku("preuzimanje.vrijeme")) * 60 * 60 * 1000;
		this.ciklusKoriekcijaCK = Integer.parseInt(pbp.dajPostavku("ciklus.korekcija"));
		this.ciklusVrijemeTC = Integer.parseInt(pbp.dajPostavku("ciklus.vrijeme")) * 1000;

		this.korime = pbp.dajPostavku("OpenSkyNetwork.korisnik");
		this.lozinka = pbp.dajPostavku("OpenSkyNetwork.lozinka");

		this.osKlijent = new OSKlijent(korime, lozinka);

		super.start();
	}

	/**
	 * Metoda za pretvaranje datuma u epoch.
	 *
	 * @param datumKonfig datum iz konfiguracijske datoteke
	 * @return vraća long parametar
	 */
	private long pretvoriDatumEpoch(String datumKonfig) {
		long epoch = -1;
		try {
			epoch = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").parse(datumKonfig).getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return epoch;
	}

	/**
	 * Metoda dohvaća trenutno vrijeme u određenom formatu.
	 *
	 * @return string trenutnog vremena
	 */
	private String dohvatiTrenutnoVrijeme() {
		String datum = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date());
		return datum;
	}

	/**
	 * Metoda za pokretanje.
	 */
	@Override
	public void run() {
		pocetakRada = dohvatiTrenutnoVrijeme();
		//Otvaranje veze prema bazi podataka
		String url = pbp.getServerDatabase() + pbp.getUserDatabase();
		String bpkorisnik = pbp.getUserUsername();
		String bplozinka = pbp.getUserPassword();
		Connection veza = null;
		try {
			Class.forName(pbp.getDriverDatabase(url));
			veza = DriverManager.getConnection(url, bpkorisnik, bplozinka);
		} catch (SQLException | ClassNotFoundException e) {
			Logger.getLogger(PreuzimanjeRasporedaAerodroma.class.getName()).log(Level.SEVERE, null, e);
			e.printStackTrace();
		}
		
		//Postavljanje inicijalnih vrijednosti za izračun vremena spavanja dretve
		int stvarniBrojacCiklusa = 0;
		int virtualniBrojacCiklusa = 0;
		long vrijemeT1 = preuzimanjeOd;
		long vrijemeT2 = preuzimanjeOd + preuzimanjeVrijemeTV;
		long vrijemePocetkaRada = System.currentTimeMillis();

		long vrijemeZavrsetkaObradeCiklusa = 0;

		while (vrijemeT1 < preuzimanjeDo) {

			//Vrši se dohvaćanje polaska i dolaska aerodroma sa servisa
			vrijemeZavrsetkaObradeCiklusa = dohvatiIObradiPodatke(vrijemeT1, vrijemeT2, veza);

			long vrijemeEfektivnogRadaTE = System.currentTimeMillis() - vrijemeZavrsetkaObradeCiklusa;

			// Povecanje brojaca (stvarni za 1, a vitualni za n)
			stvarniBrojacCiklusa++;
			// Povecavanje pocetnog i zavrsnog vremena ciklusa za
			vrijemeT1 = povecajVrijemeSljedecegCiklusa(vrijemeT1);
			vrijemeT2 = povecajVrijemeSljedecegCiklusa(vrijemeT2);

			// Provjera spavanja dretve za 1 dan
			long vrijemeSpavanjaTS = 0;
			if (vrijemeT1 > (System.currentTimeMillis() - this.preuzimanjeOdmakTO)) {
				System.out.println("Dretva mora čekati 1 dan");
				vrijemeSpavanjaTS = 24 * 60 * 60 * 1000;
				virtualniBrojacCiklusa += (int) (vrijemeSpavanjaTS / ciklusVrijemeTC);
			}
			// izracunava se vrijeme spavanja i dobivanja n za pomak virtualnog vremena
			else {
				this.n = 0;
				vrijemeSpavanjaTS = izracunVremenaSpavanja(vrijemeEfektivnogRadaTE);
				if (vrijemeEfektivnogRadaTE > this.ciklusVrijemeTC) {
					virtualniBrojacCiklusa += this.n;
				} else {
					virtualniBrojacCiklusa++;
				}
			}
			// Izracun vremena spavanja dretve kod korekcije
			if ((stvarniBrojacCiklusa % ciklusKoriekcijaCK) == 0) {
				System.out.println("Početak sljedećeg ciklusa: " + vrijemeT2);
				System.out.println("Trenutno vrijeme ciklusa: " + vrijemeT1);
				vrijemeSpavanjaTS = izracunajVrSpavanjaKorekcije(virtualniBrojacCiklusa, vrijemePocetkaRada);
			}

			System.out.println("Stvarni brojač ciklusa je trenutno na: " + stvarniBrojacCiklusa);
			System.out.println("Virtualni brojač ciklusa je trenutno na: " + virtualniBrojacCiklusa);
			System.out.println("Dretva je efektivno radila: " + vrijemeEfektivnogRadaTE + " milisekunda");
			System.out.println("Potrebno spavanje: " + vrijemeSpavanjaTS + " milisekunda");

			kratkaPauza(vrijemeSpavanjaTS);
		}

		try {
			veza.close();
		} catch (SQLException e) {
			Logger.getLogger(PreuzimanjeRasporedaAerodroma.class.getName()).log(Level.SEVERE, null, e);
			e.printStackTrace();
		}
	}

	/**
	 * Metoda dohvaća i obrađuje podatke.
	 *
	 * @param vrijemeT1 vrijeme pocetka ciklusa
	 * @param vrijemeT2 vrijeme pocetka sljedećeg ciklusa
	 * @param veza the veza
	 * @return vraća long parametar
	 */
	private long dohvatiIObradiPodatke(long vrijemeT1, long vrijemeT2, Connection veza) {
		long vrijemePocetkaCiklusa = System.currentTimeMillis();

		AerodromiPraceniDAO adao = new AerodromiPraceniDAO();

		List<Aerodrom> aerodromi = adao.dohvatiSvePraceneAerodrome(veza);
		List<AvionLeti> aerodromiPolasciDodati = new ArrayList<AvionLeti>();
		List<AvionLeti> aerodromiDolasciDodati = new ArrayList<AvionLeti>();

		aerodromiPolasciDodati.clear();
		aerodromiDolasciDodati.clear();

		AerodromiPolasciDAO apdao = new AerodromiPolasciDAO();
		AerodromiDolasciDAO addao = new AerodromiDolasciDAO();

		for (Aerodrom a : aerodromi) {
			Timestamp vrijemeOd = new Timestamp(vrijemeT1);
			Timestamp vrijemeDo = new Timestamp(vrijemeT2);

			List<AvionLeti> avioniPolasci;
			try {
				avioniPolasci = osKlijent.getDepartures(a.getIcao(), vrijemeOd, vrijemeDo);
				if (avioniPolasci != null) {
					for (AvionLeti avion : avioniPolasci) {
						aerodromiPolasciDodati.add(avion);
					}
				}
			} catch (NwtisRestIznimka e) {
				zapisiPodatkeProblema(a.getIcao(), "Problem prilikom čitanja podataka iz NWTiS - AerodromPolasci",
						veza);
			}

			List<AvionLeti> avioniDolasci;
			try {
				avioniDolasci = osKlijent.getArrivals(a.getIcao(), vrijemeOd, vrijemeDo);
				if (avioniDolasci != null) {
					for (AvionLeti avion : avioniDolasci) {
						aerodromiDolasciDodati.add(avion);
					}
				}
			} catch (NwtisRestIznimka e) {
				zapisiPodatkeProblema(a.getIcao(), "Problem prilikom čitanja podataka iz NWTiS - AerodromDolasci",
						veza);
			}

			kratkaPauza();
		}

		System.out.println("Ukupno polaska koje trebam zapisati: " + aerodromiPolasciDodati.size());
		apdao.dodajNovogAerodromaPolaska(aerodromiPolasciDodati, veza);
		System.out.println("Ukupno dolaska koje trebam zapisati: " + aerodromiDolasciDodati.size());
		addao.dodajNovogAerodromaDolaska(aerodromiDolasciDodati, veza);

		return vrijemePocetkaCiklusa;
	}

	/**
	 * Metoda zapisuje podatke za probleme.
	 *
	 * @param icao icao kod aerodroma
	 * @param opis opis problema
	 * @param veza the veza
	 */
	private void zapisiPodatkeProblema(String icao, String opis, Connection veza) {
		DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		LocalDateTime datumVrijeme = LocalDateTime.now();

		AerodromiProblemi ap = new AerodromiProblemi(icao, opis, format.format(datumVrijeme));
		AerodromiProblemiDAO apdao = new AerodromiProblemiDAO();
		apdao.dodajNoviProblem(ap, veza);
	}

	/**
	 * Metoda za kratku pauzu.
	 */
	private void kratkaPauza() {
		try {
			Thread.sleep(preuzimanjePauza);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Metoda za kratku pauzu sa dužinom.
	 *
	 * @param vrijemeSpavanjaTS vrijeme koje je potrebno da dretva spava
	 */
	private void kratkaPauza(long vrijemeSpavanjaTS) {
		try {
			Thread.sleep(vrijemeSpavanjaTS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Metoda povećava vrijeme sljedećih ciklusa.
	 *
	 * @param vrijeme trenutno vrijeme ciklusa
	 * @return vraća parametar long
	 */
	private long povecajVrijemeSljedecegCiklusa(long vrijeme) {
		return vrijeme + preuzimanjeVrijemeTV;
	}

	/**
	 * Metoda izračunava vrijeme koje je potrebno da dretva spava.
	 *
	 * @param vrijemeEfektivnogRadaTE parametar vremena efektivnog rada
	 * @return vraća parametar long
	 */
	private long izracunVremenaSpavanja(long vrijemeEfektivnogRadaTE) {
		
		if (vrijemeEfektivnogRadaTE > this.ciklusVrijemeTC) {
			for (int i = 2; i > 0; i++) {
				if ((i * this.ciklusVrijemeTC) > vrijemeEfektivnogRadaTE) {
					this.n = i;
					return (i * this.ciklusVrijemeTC) - vrijemeEfektivnogRadaTE;
				}
			}
			
		}
		return (this.ciklusVrijemeTC - vrijemeEfektivnogRadaTE);
	}

	/**
	 * Metoda računa vrijeme koje je potrebno za spavanje dretve kod korekcije.
	 *
	 * @param virtualniBrojacCiklusa parametar virtualnog brojača ciklusa
	 * @param vrijemePocetkaRada     parametar vremena početka rada dretve
	 * @return vraća parametar long
	 */
	private long izracunajVrSpavanjaKorekcije(int virtualniBrojacCiklusa, long vrijemePocetkaRada) {
		long korekcija = virtualniBrojacCiklusa * ciklusVrijemeTC;
		long trenutnoVrijemeTR = System.currentTimeMillis();
		long sljedeciCiklus = vrijemePocetkaRada + korekcija;
		System.out.println("Pocetak sljedeceg ciklusa: " + sljedeciCiklus);
		System.out.println("Trenutni ciklus:" + trenutnoVrijemeTR);
		return Math.abs(sljedeciCiklus - trenutnoVrijemeTR);
	}

	/**
	 * Metoda za prekid dretve.
	 */
	@Override
	public void interrupt() {
		super.interrupt();
	}

}
