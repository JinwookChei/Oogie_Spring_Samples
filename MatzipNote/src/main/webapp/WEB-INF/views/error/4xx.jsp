<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<c:set var="root" value="${pageContext.request.contextPath}" />
<%-- 이 화면은 BasicErrorController(/error)가 고른다: error/{코드}.jsp(예: 404.jsp)가 없는 4xx(400, 405 등)가 여기로 온다 --%>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>요청 오류</title>
</head>
<body>
	<h1>${status} - 잘못된 요청</h1>
	<p>${error}</p>
	<%-- path는 사용자가 입력한 주소 → c:out으로 HTML 이스케이프 (스크립트가 섞여 와도 글자로만 출력) --%>
	<p>요청하신 주소(<code><c:out value="${path}" /></code>)에 대한 요청을 처리할 수 없습니다. 입력한 값이나 요청 방식을 다시 확인해 주세요.</p>
	<a href="${root}/">메인으로</a>
</body>
</html>
