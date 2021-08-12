<%@ page import="model.BoardDAO" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Title</title>
</head>
<body>
    <%
        String pass = request.getParameter("password");
        int num = Integer.parseInt(request.getParameter("num"));

        // DB 연결
        BoardDAO bdao = new BoardDAO();
        String password = bdao.getPass(num);

        //기존 패스워드 값과 delete form에서 작성한 패스워드 비교
        if(pass.equals(password)) {
            // 패스워드가 둘다 같다면
            bdao.deleteBoard(num);

            response.sendRedirect("BoardList.jsp");
        }
        else {
    %>
            <script>
                alert("패스워드가 틀려서 삭제할 수 없습니다. 패스워드를 확인해주세요")
                history.go(-1)
            </script>
    <%
        }
    %>

</body>
</html>
