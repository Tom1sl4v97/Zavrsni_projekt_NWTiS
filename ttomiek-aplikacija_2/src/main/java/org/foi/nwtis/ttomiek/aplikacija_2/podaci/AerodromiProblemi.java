package org.foi.nwtis.ttomiek.aplikacija_2.podaci;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Klasa aerodromi problemi za modele.
 *
 * @author Tomislav Tomiek
 */

/**
 * Konstruktor klase.
 *
 * @param ident       string vrijednost icao koda aerodrome
 * @param description string vrijednost opisa problema
 * @param stored      string vrijednost o vrijemenu zapisa
 */
@AllArgsConstructor()
public class AerodromiProblemi {

	/**
	 * Getter.
	 *
	 * @return vraća string vrijednost ident
	 */
	@Getter

	/**
	 * Setter.
	 *
	 * @param string vrijednost ident za postavnjanje podatka
	 */
	@Setter
	String ident;

	/**
	 * Getter.
	 *
	 * @return string vrijednost opisa problema
	 */
	@Getter

	/**
	 * Setter.
	 *
	 * @param string vrijednost opisa porblema za postavljanje podatka
	 */
	@Setter
	String description;

	/**
	 * Getter.
	 *
	 * @return vraća string vrijednost o vremenu zapisa
	 */
	@Getter

	/**
	 * Setter.
	 *
	 * @param string vrijednost vremenu zapisa za postavljanje podatka
	 */
	@Setter
	String stored;
}
