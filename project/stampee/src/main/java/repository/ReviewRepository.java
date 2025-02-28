package repository;

import static config.DBConnectionUtil.*;
import static config.ConnectionClose.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import domain.Cafe;
import domain.Member;
import domain.Review;

public class ReviewRepository {
	public List<Review> findAllReviews() {
		String sql = "SELECT r.review_id, r.rating, r.contents, r.create_time, m.username, " +
			"m.member_id, m.password AS member_password, m.email AS member_email, m.phone_number, " +
			"c.cafe_id, c.name, c.address, c.password AS cafe_password, c.email AS cafe_email, c.contact " +
			"FROM review r " +
			"JOIN member m ON r.member_id = m.member_id " +
			"JOIN cafe c ON c.cafe_id = r.cafe_id "
			+ "ORDER BY r.create_time";
		return whileStatement(sql,1000_000L);
	}

	public void insertReview(Review review) {        //리뷰 생성,서비스 반영,테스트 완
		Connection conn = null;
		PreparedStatement pstmt = null;

		String sql = "insert into review (review_id, rating, contents, create_time, member_id, cafe_id) "
			+ "values (review_seq.nextval, ?, ?, ?, ?, ?)";

		try {
			conn = getConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setFloat(1, review.getRating());
			pstmt.setString(2, review.getContents());
			pstmt.setDate(3, review.getCreateTime());
			pstmt.setLong(4, review.getAuthor().getMemberId());
			pstmt.setLong(5, review.getCafe().getCafeId());
			pstmt.executeUpdate();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			close(conn, pstmt, null);
		}
	}

	public void updateReview(Review review) {        //리뷰 수정,서비스 반영,테스트 완
		Connection conn = null;
		PreparedStatement pstmt = null;
		String sql = "update review "
			+ "set rating=?, contents=? "
			+ "where review_id = ?";

		try {
			conn = getConnection();
			pstmt = conn.prepareStatement(sql);

			pstmt.setFloat(1, review.getRating());
			pstmt.setString(2, review.getContents());
			pstmt.setLong(3, review.getReviewId());

			pstmt.executeUpdate();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			close(conn, pstmt, null);
		}
	}

	public boolean deleteReviewByReviewId(long reviewId, long memberId) {
		String sql = "DELETE FROM review WHERE review_id = ? AND member_id = ?";
		try (Connection conn = getConnection();
			 PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setLong(1, reviewId);
			pstmt.setLong(2, memberId);
			int affectedRows = pstmt.executeUpdate();
			return affectedRows > 0;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public List<Review> findReviewsByMemberId(long memberId) {        //멤버별 리뷰 조회,서비스 반영,테스트 완

		String sql = "SELECT r.review_id, r.rating, r.contents, r.create_time, m.username, " +
			"m.member_id, m.password AS member_password, m.email AS member_email, m.phone_number, " +
			"c.cafe_id, c.name, c.address, c.password AS cafe_password, c.email AS cafe_email, c.contact " +
			"FROM review r " +
			"JOIN member m ON r.member_id = m.member_id " +
			"JOIN cafe c ON c.cafe_id = r.cafe_id " +
			"WHERE m.member_id = ? "
			+ "ORDER BY r.create_time";

		return whileStatement(sql,memberId);

	}

	public float cafeAvgOfRating(long cafeId) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		String sql = "SELECT c.cafe_id, ROUND(AVG(r.rating), 2) AS avg_rating "
			+ "FROM review r "
			+ "JOIN cafe c "
			+ "ON c.cafe_id = r.cafe_id "
			+ "where c.cafe_id = ?"
			+ "GROUP BY c.cafe_id ";

		try {
			conn = getConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setLong(1, cafeId);
			rs = pstmt.executeQuery();

			if (rs.next()) {
				return rs.getFloat("avg_rating");
			} else {
				return 0;
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			close(conn, pstmt, rs);
		}
	}

	public List<Review> findReviewsByCafeId(long cafeId) {            //카페리뷰조회,서비스 반영


		String sql = "SELECT r.review_id, r.rating, r.contents, r.create_time, m.username, " +
			"m.member_id, m.password AS member_password, m.email AS member_email, m.phone_number, " +
			"c.cafe_id, c.name, c.address, c.password AS cafe_password, c.email AS cafe_email, c.contact " +
			"FROM review r " +
			"JOIN member m ON r.member_id = m.member_id " +
			"JOIN cafe c ON c.cafe_id = r.cafe_id " +
			"WHERE c.cafe_id = ? "
			+ "ORDER BY r.create_time";
		return whileStatement(sql,cafeId);

	}

	public List<Review> whileStatement(String sql, Long id) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		List<Review> reviews = new ArrayList<>();
		try {

			conn = getConnection();
			pstmt = conn.prepareStatement(sql);

			if (id!=1000_000L){		//findAllReviews를 제외
				pstmt.setLong(1, id);
			}

			rs = pstmt.executeQuery();

			while (rs.next()) {
				Member member = Member.createMember(
					rs.getLong("member_id"),
					rs.getString("username"),
					rs.getString("member_password"),
					rs.getString("member_email"),
					rs.getString("phone_number")
				);

				Cafe cafe = new Cafe(
					rs.getLong("cafe_id"),
					rs.getString("name"),
					rs.getString("address"),
					rs.getString("cafe_password"),
					rs.getString("cafe_email"),
					rs.getString("contact")
				);

				Review review = new Review(
					rs.getLong("review_id"),
					rs.getInt("rating"),
					rs.getString("contents"),
					rs.getDate("create_time"),
					member,
					cafe
				);
				reviews.add(review);
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			close(conn, pstmt, rs);
		}
		return reviews;
	}
}
