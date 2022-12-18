package org.foi.nwtis.ttomiek.app_4.podaci;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Klasa Grupa za modela podataka.
 *
 * @author Tomislav Tomiek
 */

/**
 * Konstruktor klase.
 *
 * @param grupa string vrijednost grupe
 * @param naziv string vrijednost naziva grupe
 */
@AllArgsConstructor()
public class Grupa {

	/**
	 * Getter.
	 *
	 * @return vraća string vrijednost grupe
	 */
	@Getter

	/**
	 * Setter.
	 *
	 * @param string vrijednost grupe koji se postavlja
	 */
	@Setter
	String grupa;

	/**
	 * Getter.
	 *
	 * @return vraća string vrijednost naziva grupe
	 */
	@Getter

	/**
	 * Setter.
	 *
	 * @param naziv string vrijednost naziva grupe koji se postavlja
	 */
	@Setter
	String naziv;
}
