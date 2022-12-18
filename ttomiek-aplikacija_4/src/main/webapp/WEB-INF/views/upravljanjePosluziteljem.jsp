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
	<h1>Status poslužitelja</h1>
	<a
		href="${pageContext.servletContext.contextPath}/mvc/korisnici/pocetak">Početna</a>
	<br>
	<br>
	Trenutni status servera:<br><br>
	<div style="color: red; font-weigth: bold;">${requestScope.status}</div>
	<br>
	<c:if test="${requestScope.odgovorServera != null}">
		Odgovor servera: ${requestScope.odgovorServera}
	</c:if>
	<br><br>
	<form
		action="${pageContext.servletContext.contextPath}/mvc/korisnici/upravljanjePosluziteljemNaredba"
		method="POST">
		<label for="odabir">Odaberite željenu naredbu:</label>
		<select name="odabir">
			<option value="INIT">INIT</option>
			<option value="LOAD">LOAD</option>
			<option value="CLEAR">CLEAR</option>
			<option value="QUIT">QUIT</option>
		</select>
		<br><br> 
		<input type="submit" value="Izvrši naredbu" />
	</form>
</body>
</html>