<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<c:set var="root" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>서버 오류</title>
</head>
<body>
	<%-- MvcExceptionHandler.handleException()이 보여 주는 화면 (상태 코드 500) --%>
	<%-- 예외 메시지·스택 트레이스는 보여 주지 않는다 (내부 정보 노출) → 서버 로그(log.error)에만 --%>
	<h1>500 - 서버 오류</h1>
	<p>${msg}</p>
	<a href="${root}/">메인으로</a>
</body>
</html>
