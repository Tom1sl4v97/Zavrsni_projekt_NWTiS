<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Projekt iz NWTiS-a</title>
</head>
<body>
	<h1>Registracija korisnika</h1>
	<a
		href="${pageContext.servletContext.contextPath}/mvc/korisnici/pocetak">Početna</a>
	<br>
	<br>
	<div style="color: red; font-weigth: bold;">${requestScope.odgovor}</div>
	<br>
	<form
		action="${pageContext.servletContext.contextPath}/mvc/korisnici/registracijaKorisnika"
		method="POST">
		<label for="korime">Unesite korisničko ime:</label>
		<br>
		<input type="text" name="korime" />
		<br><br> 
		<label for="lozinka">Unesite lozinku:</label>
		<br>
		<input type="text" name="lozinka" />
		<br><br> 
		<label for="ime">Unesite ime:</label>
		<br>
		<input type="text" name="ime" />
		<br><br> 
		<label for="prezime">Unesite prezime:</label>
		<br>
		<input type="text" name="prezime" />
		<br><br> 
		<label for="email">Unesite email:</label>
		<br>
		<input type="text" name="email" />
		<br><br>
		<input type="submit" value="Registriraj se" />
	</form>
</body>
</html>