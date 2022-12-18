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
	<h1>Pregled korisnika</h1>
	<a
		href="${pageContext.servletContext.contextPath}/mvc/korisnici/pocetak">Početna</a>
	<br><br>
	<div style="color: red; font-weigth: bold;">${requestScope.odgovor}</div>
	<br>
	<table border="1">
		<tr>
			<th>korisničko ime</th>
			<th>ime</th>
			<th>prezime</th>
			<th>lozinka</th>
			<th>email</th>
			<c:if test="${requestScope.privilegijeAdmina}">
				<th>OBRIŠI</th>
			</c:if>
		</tr>
		<c:forEach var="korisnik" items="${requestScope.listaKorisnika}">
			<tr>
				<td>${korisnik.korIme}</td>
				<td>${korisnik.ime}</td>
				<td>${korisnik.prezime}</td>
				<td>${korisnik.lozinka}</td>
				<td>${korisnik.email}</td>
				<c:if test="${requestScope.privilegijeAdmina}">
					<td>
						<form
							action="${pageContext.servletContext.contextPath}/mvc/korisnici/obrisiTokenKorisnika"
							method="POST">
							<input type="hidden" name="korime" value="${korisnik.korIme}"/>
							<input type="submit" value="obriši token korisnika: ${korisnik.korIme}" />
						</form>
					</td>
				</c:if>
			</tr>
		</c:forEach>
	</table>
	<br><br>
	<form
		action="${pageContext.servletContext.contextPath}/mvc/korisnici/obrisiVlastitiToken"
		method="POST">
		<input type="submit" value="Obriši vlastiti token / odjava" />
	</form>
</body>
</html>