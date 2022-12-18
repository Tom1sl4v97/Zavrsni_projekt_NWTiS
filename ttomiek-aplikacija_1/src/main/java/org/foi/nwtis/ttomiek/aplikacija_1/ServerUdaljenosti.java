package org.foi.nwtis.ttomiek.aplikacija_1;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

import org.foi.nwtis.podaci.Aerodrom;
import org.foi.nwtis.ttomiek.vjezba_03.konfiguracije.Konfiguracija;
import org.foi.nwtis.ttomiek.vjezba_03.konfiguracije.KonfiguracijaApstraktna;
import org.foi.nwtis.ttomiek.vjezba_03.konfiguracije.NeispravnaKonfiguracija;

// TODO: Auto-generated Javadoc
/**
 * Klasa poslužitelja ServerUDaljenosti.
 */
public class ServerUdaljenosti {

	/** Broj porta. */
	private int port = 0;

	/** Broj maksimalnih dretvi. */
	private int maksDretvi = -1;

	/** Socket za povezivanje na poslužitelja. */
	private Socket veza = null;

	/** Konfiguracijski podaci. */
	private static Konfiguracija konfig = null;

	/** Interna kolekcija za popis aerodroma. */
	private List<Aerodrom> listaAerodroma = null;

	/** Vrijednost statusa servera. */
	private int statusServera = 0;

	/** Brojač dretvi. */
	private int brojacDretvi = 0;

	/**
	 * Konstruktor klase poslužitelja ServerUdaljenosti.
	 *
	 * @param port       varijabla porta servera
	 * @param maksDretvi broj maksimalnih dretvi
	 */
	public ServerUdaljenosti(int port, int maksDretvi) {
		this.port = port;
		this.maksDretvi = maksDretvi;
	}

	/**
	 * Main metoda koja poziva ostale metode klase ServeraUdaljenosti, čita
	 * konfiguracijsku datoteku, instancira novi objekt klase servera udaljenosti i
	 * vrši svoju metodu obrade zahtjeva.
	 * 
	 * @param args unešeni argumenti korisnika
	 */
	public static void main(String[] args) {
		int port = 0;
		int maksDretvi = -1;
		String nazivKonfDatoteke = args[0];

		if (args.length != 1) {
			System.out.println("ERROR 14 Parametar mora biti naziv konfiguracijske datoteke (postavke)!");
			return;
		}

		if (!ucitajKonfiguraciju(nazivKonfDatoteke))
			return;

		try {
			port = Integer.parseInt(konfig.dajPostavku("port"));
			maksDretvi = Integer.parseInt(konfig.dajPostavku("maks.dretvi"));
		} catch (Exception e) {
			System.out.println(
					"ERROR 14 Nisu pronađeni potrebni argumenti konfiguracijske datoteke: " + nazivKonfDatoteke);
			return;
		}
		System.out.println("Server se podiže na portu: " + port);
		ServerUdaljenosti su = new ServerUdaljenosti(port, maksDretvi);

		if (!su.provjeraKonfPodataka()) {
			System.out.println("ERROR 14 Podaci koji su zapisani u konfiguracijskoj datoteci: " + nazivKonfDatoteke
					+ " nisu valjani");
			return;
		}

		su.obradaZahtjeva();
	}

	/**
	 * Učitavanje konfiguracije.
	 *
	 * @param nazivDatoteke naziv konfiguracijske datoteke
	 * @return true ako pronađe traženu datoteku, inače false
	 */
	public static boolean ucitajKonfiguraciju(String nazivDatoteke) {
		try {
			konfig = KonfiguracijaApstraktna.preuzmiKonfiguraciju(nazivDatoteke);
		} catch (NeispravnaKonfiguracija e) {
			System.out.println("ERROR 14 Nije pronađena tražena datoteka postavki: " + nazivDatoteke);
			return false;
		}
		return true;
	}

	/**
	 * Metoda provjerava konfiguracijske podatke da li su ispravni.
	 *
	 * @return true ako su podaci ispravni, inače false
	 */
	public boolean provjeraKonfPodataka() {
		if (this.port >= 8000 && this.port <= 9999 && this.maksDretvi > 0)
			return true;
		return false;
	}

	/**
	 * Metoda čeka korisnika koji će se spojiti, te dobivenu vezu prosljeđuje
	 * novokreiranoj dretvi koja vrši i obrađuje naredbu.
	 */
	public void obradaZahtjeva() {
		try (ServerSocket ss = new ServerSocket(this.port)) {
			while (true) {
				while (this.brojacDretvi >= this.maksDretvi) {
					try {
						Thread.sleep(1000);
						System.out.println(
								"ERROR 14 Molimo Vas pričekajte, jer je dostignut maksimalan broj rada dretvi");
					} catch (InterruptedException e) {
						System.out.println(
								"ERROR 14 Dogodila se je greška prilikom čekanja u redu, jer je dostignut maksimalan broj rada dretvi");
					}
				}
				this.veza = ss.accept();
				DretvaZahtjeva dretva = new DretvaZahtjeva(veza, this);
				this.povećajBrojac();
				dretva.start();
			}

		} catch (IOException ex) {
			System.out.println("ERROR 14 Navedeni port je već zauzeti");
		}
	}

	/**
	 * Metoda povećava brojač za provjeru maksimalnog broja dretvi.
	 */
	public void povećajBrojac() {
		this.brojacDretvi++;
	}

	/**
	 * Metoda smanjuje brojač dretvi.
	 */
	public void smanjiBrojac() {
		this.brojacDretvi--;
	}

	/**
	 * Metoda vrši naredbu za brisanje svih zapisa liste aerodroma.
	 */
	public void ocistiListuAerodroma() {
		this.listaAerodroma = null;
	}

	/**
	 * Metoda postavlja listu aerodroma.
	 *
	 * @param listaAerodroma lista aerodroma za postavljanje
	 */
	public void postaviListuAerodroma(List<Aerodrom> listaAerodroma) {
		this.listaAerodroma = listaAerodroma;
	}

	/**
	 * Metoda dohvaća listu aerodroma.
	 *
	 * @return vraća listu aerodroma
	 */
	public List<Aerodrom> dohvatiListuAerodroma() {
		return this.listaAerodroma;
	}

	/**
	 * Metoda postavlja vrijednost statusa servera.
	 *
	 * @param status vrijednost statusa servera za postavljanje
	 */
	public void postaviStatusServera(int status) {
		this.statusServera = status;
	}

	/**
	 * Metoda dohvaća vrijednost statusa servera.
	 *
	 * @return vraća integer statusa servera
	 */
	public int dohvatiStatusServera() {
		return this.statusServera;
	}

	/**
	 * Metoda prekida rad servera.
	 */
	public void prekiniRadServera() {
		System.exit(0);
	}
}
