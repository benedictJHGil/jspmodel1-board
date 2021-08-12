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
    <%--  게시글 작성한 데이터를 한번에 읽어드림  --%>
    <jsp:useBean id="boardbean" class="model.BoardBean">
        <jsp:setProperty name="boardbean" property="*"/>
    </jsp:useBean>

    <%
        // DB로 빈 클래스 넘김
        BoardDAO bdao = new BoardDAO();

        // 데이터 저장 메소드 호출
        bdao.insertBoard(boardbean);

        // 게시글 저장 후 전체 게시글 보기
        response.sendRedirect("BoardList.jsp");
    %>
</body>
</html>
