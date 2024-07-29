package repository;

import static config.DBConnectionUtil.*;
import static template.ConnectionClose.*;
import static util.PasswordUtil.*;

import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import domain.Cafe;
import domain.Member;
import domain.Signature;

public class CafeRepository {
	private static final Logger log = LoggerFactory.getLogger(CafeRepository.class);

	public long cafeSignUp(Cafe cafe, Signature signature) {
		String insertCafeSql = "insert into cafe(cafe_Id, name, address, password, email, contact) "
			+ "values(CAFE_SEQ.NEXTVAL,?,?,?,?,?)";
		String insertSignatureSql = "insert into signature(menu_id, menu_name, cafe_id)"
			+ "values(SIGNATURE_SEQ.NEXTVAL, ?, ?)";

		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			conn = getConnection();
			conn.setAutoCommit(false);  // 트랜잭션 시작

			// 카페 정보 삽입
			pstmt = conn.prepareStatement(insertCafeSql, new String[]{"cafe_id"});
			pstmt.setString(1, cafe.getName());
			pstmt.setString(2, cafe.getAddress());
			pstmt.setString(3, hashPassword(cafe.getPassword()));
			pstmt.setString(4, cafe.getEmail());
			pstmt.setString(5, cafe.getContact());

			int affectedRows = pstmt.executeUpdate();

			if (affectedRows == 0) {
				throw new SQLException("Creating cafe failed, no rows affected.");
			}

			// 생성된 cafe_id 가져오기
			rs = pstmt.getGeneratedKeys();
			long cafeId;
			if (rs.next()) {
				cafeId = rs.getLong(1);
			} else {
				throw new SQLException("Creating cafe failed, no ID obtained.");
			}

			// 시그니처 메뉴 삽입을 위해 pstmt 재설정
			pstmt.close(); // 기존 pstmt 닫기
			pstmt = conn.prepareStatement(insertSignatureSql);
			pstmt.setString(1, signature.getMenuName());
			pstmt.setLong(2, cafeId);
			pstmt.executeUpdate();

			conn.commit();  // 트랜잭션 커밋
			return cafeId;
		} catch (SQLException | NoSuchAlgorithmException e) {
			if (conn != null) {
				try {
					conn.rollback();  // 예외 발생 시 롤백
				} catch (SQLException ex) {
					log.error("Rollback failed", ex);
				}
			}
			log.error("Error during cafe sign up", e);
			throw new RuntimeException("Failed to sign up cafe", e);
		} finally {
			close(conn, pstmt, rs);
		}
	}

	public boolean login(Cafe cafe){
		//sql
		String sql = "select password from cafe where email = ?";
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		//db connection
		conn = getConnection();

		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, cafe.getEmail());
			rs = pstmt.executeQuery();

			if(rs.next()){
				String storedPassword = rs.getString("password");
				return verifyPassword(cafe.getPassword(), storedPassword);
			}
			else{
				System.out.println("email not found : "+cafe.getEmail());
				return false;
			}

		} catch (SQLException | NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		} finally {
			close(conn, pstmt);
		}

	}

	public List<Member> findCafeMembersById(int cafeId) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		String sql = "select * "
			+ "from member "
			+ "where member_id in (select member_id "
			+ "                      from stamp "
			+ "                     where cafe_id = ?)";

		List<Member> members = new ArrayList<>();

		try {
			conn = getConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, cafeId);
			rs = pstmt.executeQuery();

			while (rs.next()) {
				Member member = Member.createMember(
					rs.getInt("member_id"),
					rs.getString("username"),
					rs.getString("email"),
					rs.getString("password"),
					rs.getString("phone_number")
				);
				members.add(member);
			}
			return members;
		} catch (SQLException e) {
			log.info("db error", e);
			throw new RuntimeException();
		} finally {
			close(conn, pstmt, rs);
		}
	}
}
