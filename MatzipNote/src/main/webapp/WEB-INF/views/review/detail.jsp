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
	<%-- 8-5: 없는 리뷰는 이제 MvcExceptionHandler.handleNotFound → error/404 로 간다 → 여기 오는 건 항상 review가 있을 때 (error 분기 삭제) --%>
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

	<%-- 사진이 있을 때만 표시 (없으면 src="/upload/" → 깨진 이미지 아이콘) --%>
	<%-- /upload/** 는 WebConfig.addResourceHandlers가 업로드 폴더로 연결해 준다 --%>
	<c:if test="${not empty review.imageName}">
		<div>
			<img src="${root}/upload/${review.imageName}" alt="${review.title} 사진" width="300">
		</div>
	</c:if>

	<c:if test="${review.mno == sessionScope.loginUser.mno}">
		<form action="${root}/review/${review.rno}/delete" name="delete"
			method="POST">
			<button>삭제</button>
		</form>
		<a href="${root}/review/${review.rno}/edit">수정</a>
	</c:if>

	<a href="${root}/review/list">리뷰 목록</a>
</body>
</html>