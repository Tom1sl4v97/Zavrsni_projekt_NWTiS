package org.foi.nwtis.ttomiek.aplikacija_5.wsock;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.foi.nwtis.ttomiek.aplikacija_5.dretve.Osvjezivac;

import jakarta.websocket.CloseReason;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

/**
 * Klasa Info.
 */
@ServerEndpoint("/info")
public class Info {

	/** Parametar sesije. */
	private static Set<Session> sesije = new HashSet<Session>();

	/**
	 * Metoda se vrši prilikom otvaranja sesije.
	 *
	 * @param sesija parametar sesije
	 * @param konfig parametar konfiguracije
	 */
	@OnOpen
	public void otvori(Session sesija, EndpointConfig konfig) {
		sesije.add(sesija);
		System.out.println("Otvorena sesija: " + sesija.getId());
	}

	/**
	 * Metoda zatvaranja sesije.
	 *
	 * @param sesija parametar sesije
	 * @param razlog parametar razloga
	 */
	@OnClose
	public void zatvori(Session sesija, CloseReason razlog) {
		sesije.remove(sesija);
		System.out.println("Zatvorena sesija: " + sesija.getId() + " razlog: " + razlog.getReasonPhrase());
	}

	/**
	 * Metoda koja se vrši prilikom dohvaćanja poruke.
	 *
	 * @param sesija parametar sesije
	 * @param poruka parametar poruka
	 */
	@OnMessage
	public void stiglaPoruka(Session sesija, String poruka) {
		if(poruka == "info") {
			Info.informiraj(Osvjezivac.porukaServera);
		}
		System.out.println("Sesija: " + sesija.getId() + " poruka: " + poruka);
	}

	/**
	 * Metoda se vrši prilikom izvršavanja greške.
	 *
	 * @param sesija parametar sesije
	 * @param greska parametar greške
	 */
	@OnError
	public void greska(Session sesija, Throwable greska) {
		System.out.println("Sesija: " + sesija.getId() + " greska: " + greska.getMessage());
	}

	/**
	 * Metoda šalje podatke poruke
	 *
	 * @param poruka parametar poruke
	 */
	public static void informiraj(String poruka) {
		for (Session s : sesije) {
			if (s.isOpen()) {
				try {
					s.getBasicRemote().sendText(poruka);
				} catch (IOException e) {
					System.out.println("Sesija: " + s.getId() + " greska: " + e.getMessage());
				}
			}
		}
	}

}
