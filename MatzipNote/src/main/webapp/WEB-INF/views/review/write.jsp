<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<c:set var="root" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Write Review Page</title>
</head>
<body>
	<form action="${root}/review/write" method="post">
		<div>제목: <input type="text" name="title" required></div>
		<div>내용: <textarea name="content" required></textarea></div>
		<div>별점: <input type="number" name="rating" min=1 max=5 required></div>
		<button>작성</button>
	</form>
</body>
</html>