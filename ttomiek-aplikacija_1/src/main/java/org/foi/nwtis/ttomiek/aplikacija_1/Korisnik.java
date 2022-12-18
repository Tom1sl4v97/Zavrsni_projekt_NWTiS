package org.foi.nwtis.ttomiek.aplikacija_1;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.foi.nwtis.podaci.Aerodrom;
import org.foi.nwtis.rest.podaci.Lokacija;

import com.google.gson.Gson;

public class Korisnik {

	public static void main(String[] args) {
		String zahtjev = "";
		for (int i = 0; i < args.length; i++) {
			if (i == (args.length - 1))
				zahtjev += args[i];
			else
				zahtjev += args[i] + " ";
		}

		if (args[0].equals("LOAD")) {
			List<Aerodrom> aerodromi = new ArrayList<>();
			Aerodrom ad = new Aerodrom("LDZA", "Airport Zagreb", "HR", new Lokacija("45.743056", "16.068889"));
			aerodromi.add(ad);
			ad = new Aerodrom("LDVA", "Airport VaraĹľdin", "HR", new Lokacija("46.2946472", "16.3829327"));
			aerodromi.add(ad);
			ad = new Aerodrom("EDDF", "Airport Frankfurt", "DE", new Lokacija("0", "0"));
			aerodromi.add(ad);
			ad = new Aerodrom("EDDB", "Airport Berlin", "DE", new Lokacija("0", "0"));
			aerodromi.add(ad);
			ad = new Aerodrom("LOWW", "Airport Vienna", "AT", new Lokacija("48.1102982", "16.5697002"));
			aerodromi.add(ad);

			
			Gson gson = new Gson();
			String json = gson.toJson(aerodromi);
			zahtjev += " " + json;
		}

		Korisnik korisnik = new Korisnik();
		String odgovor = korisnik.posaljiKomandu("localhost", 8002, zahtjev);
		korisnik.ispis(odgovor);
	}

	public String posaljiKomandu(String adresa, int port, String komanda) {
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
			ispis(e.getMessage());
		} catch (IOException ex) {
			ispis(ex.getMessage());
		}
		return null;
	}

	private void ispis(String message) {
		System.out.println(message);
	}
}