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
	<%-- 작성 실패(이미지가 아닌 파일 등) 후 redirect로 돌아왔을 때 flash msg 표시 --%>
	<c:if test="${not empty msg}">
		<p style="color: red">${msg}</p>
	</c:if>
	<form action="${root}/review/write" method="post" enctype="multipart/form-data">
		<div>제목: <input type="text" name="title" required></div>
		<div>내용: <textarea name="content" required></textarea></div>
		<div>별점: <input type="number" name="rating" min=1 max=5 required></div>
		<div>사진: <input type="file" name="image" accept="image/*"></div>
		<button>작성</button>
	</form>
</body>
</html>