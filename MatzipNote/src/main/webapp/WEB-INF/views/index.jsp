<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<c:set var="root" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Index Page</title>
</head>
<body>
	<h1>맛집 노트!</h1>
	
	 <c:choose>
	 	<c:when test="${empty loginUser}">
	 		<a href="${root}/member/signup">회원가입</a>
			<a href="${root}/member/login">로그인</a>
	 	</c:when>
	 	
	 	<c:otherwise>
	 		<a>${loginUser.name}님 환영합니다^^</a>
	 		<br>
	 		<a href="${root}/review/list">리뷰 목록</a>
	 		<form action="${root}/member/logout" method="post">
	 			<button>로그아웃</button>
	 		</form>
	 	</c:otherwise>
	 </c:choose>
	
</body>
</html>