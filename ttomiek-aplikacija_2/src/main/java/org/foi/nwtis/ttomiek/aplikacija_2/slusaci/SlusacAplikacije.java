package org.foi.nwtis.ttomiek.aplikacija_2.slusaci;

import java.io.File;

import org.foi.nwtis.ttomiek.aplikacija_2.dretve.PreuzimanjeRasporedaAerodroma;
import org.foi.nwtis.ttomiek.vjezba_03.konfiguracije.NeispravnaKonfiguracija;
import org.foi.nwtis.ttomiek.vjezba_06.konfiguracije.bazaPodataka.KonfiguracijaBP;
import org.foi.nwtis.ttomiek.vjezba_06.konfiguracije.bazaPodataka.PostavkeBazaPodataka;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

/**
 * Klasa slušača aplikacije.
 */
@WebListener
public class SlusacAplikacije implements ServletContextListener {

	/** Varijabla za pohranu preuzimanja rasporeda aerodroma. */
	PreuzimanjeRasporedaAerodroma pra = null;

	/**
	 * Konstruktor klase.
	 */
	public SlusacAplikacije() {

	}

	/**
	 * Context inicijalizacija.
	 *
	 * @param sce ServletContextEvent parametar
	 */
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		ServletContext context = sce.getServletContext();
		String nazivDatoteke = context.getInitParameter("konfiguracija");
		String putanja = context.getRealPath("/WEB-INF");
		nazivDatoteke = putanja + File.separator + nazivDatoteke;

		KonfiguracijaBP konfig = new PostavkeBazaPodataka(nazivDatoteke);
		try {
			konfig.ucitajKonfiguraciju();
		} catch (NeispravnaKonfiguracija e) {
			e.printStackTrace();
			return;
		}

		context.setAttribute("Postavke", konfig);

		this.pra = new PreuzimanjeRasporedaAerodroma((PostavkeBazaPodataka) context.getAttribute("Postavke"));
		pra.start();
		ServletContextListener.super.contextInitialized(sce);
	}

	/**
	 * Context za uništavanje.
	 *
	 * @param sce parametar za ServletContextEvent
	 */
	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		this.pra.interrupt();
		ServletContext context = sce.getServletContext();
		context.removeAttribute("Postavke");
		System.out.println("postavke obrisane!");
		ServletContextListener.super.contextDestroyed(sce);
	}
}
