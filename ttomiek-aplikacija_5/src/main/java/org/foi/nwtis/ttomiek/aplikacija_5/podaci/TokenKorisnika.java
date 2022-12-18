package org.foi.nwtis.ttomiek.aplikacija_5.podaci;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Klasa token korisnika za modele podataka.
 *
 * @author Tomislav Tomiek
 */

/**
 * KOnstruktor klase tokena korisnika.
 *
 * @param id        int vrijednost ID-a tokena
 * @param korisnik  string vrijednost
 * @param status    int vrijednost statusa tokena
 * @param vrijediDo long vrijednost vrijeme isticanja tokena / epoch
 */
@AllArgsConstructor()
public class TokenKorisnika {

	/**
	 * Getter.
	 *
	 * @return vraća int vrijednost ID-a tokena
	 */
	@Getter

	/**
	 * Setter.
	 *
	 * @param id int vrijednost ID-a tokena za postavljanje
	 */
	@Setter
	int id;

	/**
	 * Getter.
	 *
	 * @return vraća string vrijednost korisničkog imena
	 */
	@Getter

	/**
	 * Setter.
	 *
	 * @param korisnik string vrijednost korisničkog imena za postavljanje
	 */
	@Setter
	String korisnik;

	/**
	 * Getter.
	 *
	 * @return vraća int vrijednost statusa tokena
	 */
	@Getter

	/**
	 * Setter.
	 *
	 * @param status int vrijednost statusa tokena za postavljanje
	 */
	@Setter
	int status;

	/**
	 * Getter.
	 *
	 * @return vraća long vrijednost epoch isticanja tokena
	 */
	@Getter

	/**
	 * Setter.
	 *
	 * @param vrijediDo long vrijednost epoch isticanja tokena za postavljanje
	 */
	@Setter
	long vrijediDo;
}
