<%@ page import="model.BoardDAO" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Title</title>
</head>
<body>
    <%
        request.setCharacterEncoding("utf-8");
    %>
    <%--데이터를 한번에 받아오는 빈 클래스를 사용--%>
    <jsp:useBean id="boardbean" class="model.BoardBean">
        <jsp:setProperty name="boardbean" property="*"/>
    </jsp:useBean>
    <%
        // DB 객체 생성
        BoardDAO bdao = new BoardDAO();
        bdao.rewriteBoard(boardbean);

        // 답변 데이터를 모두 저장한 후 전체 게시글 보기를 설정
        response.sendRedirect("BoardList.jsp");
    %>
</body>
</html>
