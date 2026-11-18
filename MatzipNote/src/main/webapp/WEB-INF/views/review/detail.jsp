<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<c:set var="root" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Review Detail</title>
</head>
<body>
	<c:choose>
		<c:when test="${not empty error}">
			<p style="color: red">${error}</p>
		</c:when>
		<c:otherwise>
			<c:if test="${not empty msg}">
				<p style="color: green">${msg}</p>	
			</c:if>
			
			<table border="1">
				<thead>
					<tr>
						<th>제목</th>
						<th>본문</th>
						<th>별점</th>
						<th>작성자</th>
						<th>작성일</th>
					</tr>
				</thead>
				<tbody>
					<tr>
						<td>${review.title}</td>
						<td>${review.content}</td>
						<td>${review.rating}점</td>
						<td>${review.writer.name}</td>
						<td>${review.createdAt}</td>
					</tr>
				</tbody>
			</table>
		</c:otherwise>
	</c:choose>
	<a href="${root}/review/list">리뷰 목록</a>
</body>
</html>