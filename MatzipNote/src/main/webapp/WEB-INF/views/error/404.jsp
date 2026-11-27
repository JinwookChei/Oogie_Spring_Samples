<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<c:set var="root" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>페이지를 찾을 수 없음</title>
</head>
<body>
	<%-- BasicErrorController가 자동으로 고르는 화면: views/error/404.jsp (상태 코드 404) --%>
	<%-- ControllerAdvice가 아님 → path, status 같은 값은 BasicErrorController의 ErrorAttributes가 넣어 준다 --%>
	<h1>404 - 페이지를 찾을 수 없음</h1>
	<%-- path는 사용자가 입력한 주소 → c:out으로 HTML 이스케이프 (스크립트가 섞여 와도 글자로만 출력) --%>
	
	<c:choose>
		<c:when test="${msg != null}">
			<p><code><c:out value="${msg}" /></code></p>
		</c:when>
		<c:otherwise>
			<p>요청하신 페이지(<code><c:out value="${path}" /></code>)가 없습니다. 주소를 다시 확인해 주세요.</p>	
		</c:otherwise>
	</c:choose>
	
	<a href="${root}/">메인으로</a>
</body>
</html>
