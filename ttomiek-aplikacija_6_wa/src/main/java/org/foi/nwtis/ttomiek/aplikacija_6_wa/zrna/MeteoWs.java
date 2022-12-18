package org.foi.nwtis.ttomiek.aplikacija_6_wa.zrna;

import org.foi.nwtis.ttomiek.ws.meteo.Meteo;
import org.foi.nwtis.ttomiek.ws.meteo.MeteoPodaci;
import org.foi.nwtis.ttomiek.ws.meteo.WsMeteo;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import jakarta.xml.ws.WebServiceRef;

/**
 * Klasa MeteoWs.
 */
@RequestScoped
@Named("meteoWs")
public class MeteoWs {

	/** Parametar web servlet kontekst. */
	@WebServiceRef(wsdlLocation = "http://localhost:9090/ttomiek-zadaca_3_wa_1/meteo?wsdl")
	private Meteo service;
	
	/** Parametar odgovor. */
	private String odgovor = "";
	
	/** Parametar icao. */
	private String icao;
	
	/** Parametar mp za meteo podatke. */
	private MeteoPodaci mp = new MeteoPodaci();

	/**
	 * Metoda dohvaća meteo podatke.
	 *
	 * @param icao parametar icao
	 * @return vraća meteo podatke
	 */
	public MeteoPodaci dajMeteoPodatke(String icao) {
		if (service == null)
			service = new Meteo();
		WsMeteo wsMeteo = service.getWsMeteoPort();
		MeteoPodaci mp = wsMeteo.dajMeteo(icao);
		if (mp == null)
			this.odgovor = "Krivo Ste unijeli icao kod aerodroma!";
		return mp;
	}

	/**
	 * Metoda dohvaća odgovor.
	 *
	 * @return vraća odgovor
	 */
	public String getOdgovor() {
		return odgovor;
	}

	/**
	 * Metoda dohvaća icao.
	 *
	 * @return vraća icao
	 */
	public String getIcao() {
		return icao;
	}

	/**
	 * Metoda postavlja icao.
	 *
	 * @param icao parametar icao
	 */
	public void setIcao(String icao) {
		this.icao = icao;
	}

	/**
	 * Metoda dohvaća meteo podatke.
	 *
	 * @return vraća meteo podatke
	 */
	public MeteoPodaci getDajMeteo() {
		System.out.println("ICAO " + icao);
		if (icao != null)
			this.mp = dajMeteoPodatke(icao);
		return mp;
	}

	/**
	 * MEtoda postavlja meteo podatke.
	 *
	 * @param mp parametar meteo podataka
	 */
	public void setDajMeteo(MeteoPodaci mp) {
		this.mp = mp;
	}

}
