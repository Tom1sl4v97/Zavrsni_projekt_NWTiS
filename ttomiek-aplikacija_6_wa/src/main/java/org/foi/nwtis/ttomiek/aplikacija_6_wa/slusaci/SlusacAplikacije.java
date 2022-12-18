package org.foi.nwtis.ttomiek.aplikacija_6_wa.slusaci;

import java.io.File;

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

	/** Parametar postavke baze podataka. */
	public static PostavkeBazaPodataka pbp;

	/**
	 * Konstruktor klase.
	 */
	public SlusacAplikacije() {

	}

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

		KonfiguracijaBP konfig = new PostavkeBazaPodataka(nazivDatoteke);
		try {
			konfig.ucitajKonfiguraciju();
		} catch (NeispravnaKonfiguracija e) {
			e.printStackTrace();
			return;
		}

		SlusacAplikacije.pbp = (PostavkeBazaPodataka) konfig;

		System.out.println("Konfiguracija učitana!");
		context.setAttribute("Postavke", konfig);

		ServletContextListener.super.contextInitialized(sce);
	}

	/**
	 * Metoda za zatvaranje konteksta servleta.
	 *
	 * @param sce parametar sce za servlet kontekst event
	 */
	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		ServletContext context = sce.getServletContext();
		context.removeAttribute("Postavke");
		System.out.println("postavke obrisane!");
		ServletContextListener.super.contextDestroyed(sce);
	}
}
