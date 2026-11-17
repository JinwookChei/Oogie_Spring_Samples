<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<c:set var="root" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>로그인</title>
</head>
<body>
        <h1>로그인</h1>

        <%-- Controller가 Model에 "error"를 담았을 때만 보임 --%>
        <c:if test="${not empty error}">
                <p style="color: red">${error}</p>
        </c:if>

        <%-- action = 보낼 URL, method = POST / input의 name = Member의 필드 이름 --%>
        <form action="${root}/member/login" method="post">
                <div>이메일 <input type="email" name="email" required></div>
                <div>비밀번호 <input type="password" name="password" required></div>
                <button>로그인</button>
        </form>

        <a href="${root}/">메인으로</a>
</body>
</html>