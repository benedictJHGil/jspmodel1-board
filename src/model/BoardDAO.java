package model;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Vector;

public class BoardDAO {

    Connection con = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;

    private void getCon() {

        try {
            Context initctx = new InitialContext();
            Context envctx = (Context) initctx.lookup("java:comp/env");
            DataSource ds = (DataSource) envctx.lookup("jdbc/pool");

            con = ds.getConnection();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // 하나의 새로운 게시글이 넘어와서 저자오디는 메소드
    public void insertBoard(BoardBean bean) {

        getCon();
        // 빈 클래스에 null인 데이터 초기화
        int ref = 0; // 글 그룹: 쿼리를 실행시켜서 가장 큰 ref값을 가져온 후 +1
        int re_step = 1; // 새글 = 부모글
        int re_level = 1;

        try {
            // 가장 큰 ref값을 읽어오는 쿼리
            String refSql = "select max(ref) from board";
            // 쿼리 실행 객체
            pstmt = con.prepareStatement(refSql);
            // 쿼리 실행 후 결과 반환
            rs = pstmt.executeQuery();
            if(rs.next()) { // 결과값이 있다면
                ref = rs.getInt(1) + 1; // 최대값이 +1을 해서 글 그룹을 설정
            }
            // 실제로 게시글 전체값을 테이블에 저장
            String sql = "insert into board values(board_seq.NEXTVAL, ?, ?, ?, ?, sysdate, ?, ?, ?, 0, ?)";
            pstmt = con.prepareStatement(sql);
            // ?에 값을 맵핑
            pstmt.setString(1, bean.getWriter());
            pstmt.setString(2, bean.getEmail());
            pstmt.setString(3, bean.getSubject());
            pstmt.setString(4, bean.getPassword());
            pstmt.setInt(5, ref);
            pstmt.setInt(6, re_step);
            pstmt.setInt(7, re_level);
            pstmt.setString(8, bean.getContent());
            // 쿼리 실행
            pstmt.executeUpdate();
            // 자원 반납
            con.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // 모든 게시글을 반환해주는 메소드
    public Vector<BoardBean> getAllBoard(int start, int end) {

        // 반환할 객체 선언
        Vector<BoardBean> v = new Vector<>();
        getCon();

        try {
            // 쿼리 준비
            String sql = "select * from (select A.*, Rownum Rnum from (select * from board order by ref desc, re_step asc) A)" + "where Rnum >= ? and Rnum <= ?";
            // 쿼리를 실행할 객체 선언
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, start);
            pstmt.setInt(2, end);
            // 쿼리 실행 후 결과 저장
            rs = pstmt.executeQuery();
            // 데이터 추출
            while(rs.next()) {
                // 데이터를 패키징
                BoardBean bean = new BoardBean();
                bean.setNum(rs.getInt(1));
                bean.setWriter(rs.getString(2));
                bean.setEmail(rs.getString(3));
                bean.setSubject(rs.getString(4));
                bean.setPassword(rs.getString(5));
                bean.setReg_date(rs.getDate(6).toString());
                bean.setRef(rs.getInt(7));
                bean.setRe_step(rs.getInt(8));
                bean.setRe_level(rs.getInt(9));
                bean.setReadcount(rs.getInt(10));
                bean.setContent(rs.getString(11));
                // 패키징한 데이터를 벡터에 저장
                v.add(bean);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        return v;
    }

    // boardinfo 하나의 게시글을 반환하는 메소드
    public BoardBean getOneBoard(int num) {

        // 반환 타입 선언
        BoardBean bean = new BoardBean();
        getCon();

        try {
            // 조회수 증가 쿼리
            String readsql = "update board set readcount = readcount + 1 where num = ?";
            pstmt = con.prepareStatement(readsql);
            pstmt.setInt(1, num);
            pstmt.executeUpdate();

            // 쿼리 준비
            String sql = "select * from board where num = ?";
            // 쿼리 실행 객체
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, num);
            // 쿼리 실행 후 결과 반환
            rs = pstmt.executeQuery();
            if(rs.next()) {
                bean.setNum(rs.getInt(1));
                bean.setWriter(rs.getString(2));
                bean.setEmail(rs.getString(3));
                bean.setSubject(rs.getString(4));
                bean.setPassword(rs.getString(5));
                bean.setReg_date(rs.getDate(6).toString());
                bean.setRef(rs.getInt(7));
                bean.setRe_step(rs.getInt(8));
                bean.setRe_level(rs.getInt(9));
                bean.setReadcount(rs.getInt(10));
                bean.setContent(rs.getString(11));
            }

            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bean;
    }

    // 답변글이 저장되는 메소드
    public void rewriteBoard(BoardBean bean) {

        //부모글 그룹과 글 레벨 글 스텝을 일어드림
        int ref = bean.getRef();
        int re_step = bean.getRe_step();
        int re_level = bean.getRe_level();

        getCon();

        try {
            // (핵심) 부모 글보다 큰 re_level의 값을 전부 1씩 증가시켜줌
            String levelsql = "update board set re_level = re_level + 1 where ref = ? and re_level > ?";
            // 쿼리 실행 객체
            pstmt = con.prepareStatement(levelsql);
            pstmt.setInt(1, ref);
            pstmt.setInt(2, re_level);
            // 쿼리 실행
            pstmt.executeUpdate();
            // 답변글 데이터를 저장
            String sql = "insert into board values(board_seq.NEXTVAL, ?, ?, ?, ?, sysdate, ?, ?, ?, 0, ?)";
            pstmt = con.prepareStatement(sql);
            // ?에 값을 대입
            pstmt.setString(1, bean.getWriter());
            pstmt.setString(2, bean.getEmail());
            pstmt.setString(3, bean.getSubject());
            pstmt.setString(4, bean.getPassword());
            pstmt.setInt(5, ref); // 부모의 ref값을 넣어줌
            pstmt.setInt(6, re_step + 1); // 답글이기에 부모 글에 1을 더해줌
            pstmt.setInt(7, re_level + 1);
            pstmt.setString(8, bean.getContent());
            pstmt.executeUpdate();

            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // boardupdate, boarddelete시 하나의 게시글을 반환
    public BoardBean getOneUpdateBoard(int num) {

        // 반환 타입 선언
        BoardBean bean = new BoardBean();
        getCon();

        try {
            // 쿼리 준비
            String sql = "select * from board where num = ?";
            // 쿼리 실행 객체
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, num);
            // 쿼리 실행 후 결과 반환
            rs = pstmt.executeQuery();
            if(rs.next()) {
                bean.setNum(rs.getInt(1));
                bean.setWriter(rs.getString(2));
                bean.setEmail(rs.getString(3));
                bean.setSubject(rs.getString(4));
                bean.setPassword(rs.getString(5));
                bean.setReg_date(rs.getDate(6).toString());
                bean.setRef(rs.getInt(7));
                bean.setRe_step(rs.getInt(8));
                bean.setRe_level(rs.getInt(9));
                bean.setReadcount(rs.getInt(10));
                bean.setContent(rs.getString(11));
            }

            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bean;
    }

    // update와 delete시 필요한 패스워드 값을 반환해주는 메소드
    public String getPass(int num) {

        // 반환할 변수 객체 선언
        String pass = "";
        getCon();

        try {
            // 쿼리 준비
            String sql = "select password from board where num = ?";
            // 쿼리 실행 객체 선언
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, num);
            rs = pstmt.executeQuery();
            // 패스워드 값을 저장
            if(rs.next()) {
                pass = rs.getString(1);
            }

            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return pass;
    }

    // 하나의 게시글을 수정하는 메소드
    public void updateBoard(BoardBean bean) {

        getCon();

        try {
            // 쿼리 준비
            String sql = "update board set subject = ?, content = ? where num = ?";
            pstmt = con.prepareStatement(sql);
            // ?값을 대입
            pstmt.setString(1, bean.getSubject());
            pstmt.setString(2, bean.getContent());
            pstmt.setInt(3, bean.getNum());
            pstmt.executeUpdate();

            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // 하나의 게시글을 삭제하는 메소드
    public void deleteBoard(int num) {

        getCon();

        try {
            // 쿼리 준비
            String sql = "delete from board where num = ?";
            pstmt = con.prepareStatement(sql);
            // ?값
            pstmt.setInt(1, num);
            // 쿼리 실행
            pstmt.executeUpdate();

            con.close();
        } catch (Exception e) {

        }
    }

    // 전체 글의 갯수를 반환하는 메소드
    public int getAllCount() {

        getCon();
        // 게시글 전체 수를 저장하는 변수
        int count = 0;

        try {
            // 쿼리 준비
            String sql = "select count(*) from board";
            // 쿼리를 실행할 객체 선언
            pstmt = con.prepareStatement(sql);
            // 쿼리를 실행 후 결과 반환
            rs = pstmt.executeQuery();
            if(rs.next()) {
                count = rs.getInt(1); // 전체 게시글 수
            }
            con.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }

}
