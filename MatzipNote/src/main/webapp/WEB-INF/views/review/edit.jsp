<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<c:set var="root" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Edit Review Page</title>
</head>
<body>
	<%-- 수정 실패(이미지가 아닌 파일 등) 후 redirect로 돌아왔을 때 flash msg 표시 --%>
	<c:if test="${not empty msg}">
		<p style="color: red">${msg}</p>
	</c:if>

	<%-- 파일을 보내므로 enctype 필수 (없으면 파일 이름만 전송 → image가 null) --%>
	<form action="${root}/review/${review.rno}/edit" method="post" enctype="multipart/form-data">
		<div>제목: <input type="text" name="title" value="${review.title}" required></div>
		<div>내용: <textarea name="content" required>${review.content}</textarea></div>
		<div>별점: <input type="number" name="rating" value="${review.rating}" min=1 max=5 required></div>

		<%-- 현재 사진 미리 보기 (원래 이름은 hidden으로 보내지 않는다 → Service가 DB 원본에서 가져옴) --%>
		<c:if test="${not empty review.imageName}">
			<div>
				현재 사진:<br>
				<img src="${root}/upload/${review.imageName}" alt="${review.title} 사진" width="150">
			</div>
		</c:if>
		<div>
			사진 변경: <input type="file" name="image" accept="image/*">
			<small>(바꾸지 않으려면 비워 두세요)</small>
		</div>

		<button>수정</button>
	</form>
</body>
</html>
