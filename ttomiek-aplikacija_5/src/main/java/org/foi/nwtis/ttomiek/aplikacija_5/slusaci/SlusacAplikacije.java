package org.foi.nwtis.ttomiek.aplikacija_5.slusaci;

import java.io.File;

import org.foi.nwtis.ttomiek.aplikacija_5.dretve.Osvjezivac;
import org.foi.nwtis.ttomiek.vjezba_03.konfiguracije.NeispravnaKonfiguracija;
import org.foi.nwtis.ttomiek.vjezba_06.konfiguracije.bazaPodataka.KonfiguracijaBP;
import org.foi.nwtis.ttomiek.vjezba_06.konfiguracije.bazaPodataka.PostavkeBazaPodataka;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

/**
 * Klasa SlusacAplikacije.
 */
@WebListener
public class SlusacAplikacije implements ServletContextListener {

	/** Parametar konfig. */
	private static KonfiguracijaBP konfig;

	/**
	 * Konstruktor klase slusaca aplikacije.
	 */
	public SlusacAplikacije() {

	}

	/** Parametar pra. */
	private Osvjezivac osvjezivac;

	/**
	 * Konstruktor konteksta.
	 *
	 * @param sce parametar scr za servlet kontekst event
	 */
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		ServletContext context = sce.getServletContext();
		String nazivDatoteke = context.getInitParameter("konfiguracija");
		String putanja = context.getRealPath("/WEB-INF");
		nazivDatoteke = putanja + File.separator + nazivDatoteke;

		System.out.println(nazivDatoteke);

		konfig = new PostavkeBazaPodataka(nazivDatoteke);
		try {
			konfig.ucitajKonfiguraciju();
		} catch (NeispravnaKonfiguracija e) {
			e.printStackTrace();
			return;
		}

		System.out.println("Konfiguracija učitana!");
		context.setAttribute("Postavke", konfig);

		this.osvjezivac = new Osvjezivac();
		osvjezivac.start();

		ServletContextListener.super.contextInitialized(sce);
	}

	/**
	 * Metoda vraža parametar za postavke baze podataka.
	 *
	 * @return vraća parametar za postavke baze podataka
	 */
	public static PostavkeBazaPodataka dohvatiPBP() {
		return (PostavkeBazaPodataka) konfig;
	}

	/**
	 * Metoda za zatvaranje konteksta servleta.
	 *
	 * @param sce parametar sce za servlet kontekst event
	 */
	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		this.osvjezivac.interrupt();
		ServletContext context = sce.getServletContext();
		context.removeAttribute("Postavke");
		System.out.println("postavke obrisane!");
		ServletContextListener.super.contextDestroyed(sce);
	}
}
