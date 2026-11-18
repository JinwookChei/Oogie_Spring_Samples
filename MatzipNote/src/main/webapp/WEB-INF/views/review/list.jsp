<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<c:set var="root" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>리뷰 리스트</title>
</head>

<body>
	<form action="${root}/review/list" method="GET">
		<!-- 텍스트 입력 (아이디) -->
		<div>
			<label for="keyword">키워드:</label> <input type="text" id="keyword"
				name="keyword">
		</div>
		<!-- 최소 평점 -->
		<div>
			<label for="minRating">최소 평점:</label> <input type="number"
				id="minRating" name="minRating">
		</div>
		<button type="submit">검색</button>
	</form>

	<c:choose>
		<%-- 1. 결과가 0건(비어있는 경우)일 때 --%>
		<c:when test="${empty reviewList}">
			<p>검색 결과가 없습니다.</p>
		</c:when>

		<%-- 2. 결과가 1건 이상 존재할 때 --%>
		<c:otherwise>
			<table border="1">
				<thead>
					<tr>
						<th>번호</th>
						<th>제목</th>
						<th>별점</th>
						<th>작성일</th>
					</tr>
				</thead>
				<tbody>
					<%-- c:forEach로 목록 출력, varStatus로 번호 제어 --%>
					<c:forEach var="item" items="${reviewList}" varStatus="status">
						<tr>
							<%-- 번호 (status.count는 1부터 시작) --%>
							<td>${status.count}</td>
							<td><a href="${root}/review/${item.rno}">${item.title}</a></td>
							<td>${item.rating}점</td>
							<td>${item.createdAt}</td>
						</tr>
					</c:forEach>
				</tbody>
			</table>
		</c:otherwise>
	</c:choose>

	<a href="${root}/">메인으로</a>
	<a href="${root}/review/write">리뷰 쓰기</a>
</body>
</html>