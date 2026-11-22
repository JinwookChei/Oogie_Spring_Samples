<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<c:set var="root" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>접근 권한 없음</title>
</head>
<body>
	<%-- MvcExceptionHandler.handleForbidden()이 보여 주는 화면 (상태 코드 403) --%>
	<h1>403 - 접근 권한 없음</h1>
	<p>이 작업을 할 권한이 없습니다.</p>
	<%-- msg: ForbiddenException의 메시지 (사용자용 문구) --%>
	<c:if test="${not empty msg}">
		<p style="color: red">${msg}</p>
	</c:if>
	<a href="${root}/review/list">리뷰 목록으로</a>
</body>
</html>
