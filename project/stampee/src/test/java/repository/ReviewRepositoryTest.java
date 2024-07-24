package repository;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import domain.Cafe;
import domain.Member;
import domain.Review;

class ReviewRepositoryTest {

	private ReviewRepository reviewRepository;

	@Mock
	private Connection mockConnection;

	@Mock
	private PreparedStatement mockPreparedStatement;

	@Mock
	private ResultSet mockResultSet;

	@BeforeEach
	void setUp() throws SQLException {
		MockitoAnnotations.openMocks(this);
		reviewRepository = new ReviewRepository() {
			@Override
			protected Connection getConnection() {
				return mockConnection;
			}
		};
		when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
		when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
	}



	@Test
	public void testFindAllReviews() throws SQLException {
		// 설정 데이터
		when(mockResultSet.next()).thenReturn(true, true, false);
		when(mockResultSet.getLong("review_id")).thenReturn(1L, 2L);
		when(mockResultSet.getInt("rating")).thenReturn(5, 4);
		when(mockResultSet.getString("contents")).thenReturn("Excellent", "Good");
		when(mockResultSet.getDate("createTime")).thenReturn(new java.sql.Date(System.currentTimeMillis()));
		when(mockResultSet.getLong("member_id")).thenReturn(1L, 2L);
		when(mockResultSet.getString("password")).thenReturn("password123", "password456");
		when(mockResultSet.getString("email")).thenReturn("member@example.com", "another@example.com");
		when(mockResultSet.getString("phone_number")).thenReturn("123456789", "987654321");
		when(mockResultSet.getLong("cafe_id")).thenReturn(1L, 2L);
		when(mockResultSet.getString("name")).thenReturn("Cafe One", "Cafe Two");
		when(mockResultSet.getString("address")).thenReturn("Address One", "Address Two");
		when(mockResultSet.getString("password_1")).thenReturn("cafePass1", "cafePass2");
		when(mockResultSet.getString("email_1")).thenReturn("cafe1@example.com", "cafe2@example.com");
		when(mockResultSet.getString("contact")).thenReturn("111-1111", "222-2222");

		// 메서드 호출 및 검증
		List<Review> reviews = reviewRepository.findAllReviews();
		assertEquals(2, reviews.size());

		// 첫 번째 Review 검증
		Review review1 = reviews.get(0);
		assertEquals(1L, review1.getId());
		assertEquals(5, review1.getRating());
		assertEquals("Excellent", review1.getContents());

		// 두 번째 Review 검증
		Review review2 = reviews.get(1);
		assertEquals(2L, review2.getId());
		assertEquals(4, review2.getRating());
		assertEquals("Good", review2.getContents());
	}

	@Test
	public void testDeleteReviewByReviewId() throws SQLException {
		long reviewId = 1L;

		// 삭제 쿼리 실행 시 아무 예외도 발생하지 않도록 설정
		when(mockPreparedStatement.executeUpdate()).thenReturn(1);

		// 리뷰 삭제
		assertDoesNotThrow(() -> reviewRepository.deleteReviewByReviewId(reviewId));

		// mockPreparedStatement의 setLong과 executeUpdate 메서드가 호출되었는지 검증
		verify(mockPreparedStatement, times(1)).setLong(1, reviewId);
		verify(mockPreparedStatement, times(1)).executeUpdate();

		// mockPreparedStatement와 mockConnection의 close 메서드가 호출되었는지 검증
		verify(mockPreparedStatement, times(1)).close();
		verify(mockConnection, times(1)).close();
	}

	@Test
	public void testFindReviewById() throws SQLException {
		long reviewId = 1L;

		when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
		when(mockResultSet.next()).thenReturn(true).thenReturn(false);
		when(mockResultSet.getLong("review_id")).thenReturn(reviewId);
		when(mockResultSet.getInt("rating")).thenReturn(5);
		when(mockResultSet.getString("contents")).thenReturn("Excellent");
		when(mockResultSet.getDate("createTime")).thenReturn(new java.sql.Date(System.currentTimeMillis()));
		when(mockResultSet.getLong("member_id")).thenReturn(1L);
		when(mockResultSet.getString("password")).thenReturn("password123");
		when(mockResultSet.getString("email")).thenReturn("member@example.com");
		when(mockResultSet.getString("phone_number")).thenReturn("123456789");
		when(mockResultSet.getLong("cafe_id")).thenReturn(1L);
		when(mockResultSet.getString("name")).thenReturn("Cafe One");
		when(mockResultSet.getString("address")).thenReturn("Address One");
		when(mockResultSet.getString("password_1")).thenReturn("cafePass1");
		when(mockResultSet.getString("email_1")).thenReturn("cafe1@example.com");
		when(mockResultSet.getString("contact")).thenReturn("111-1111");

		Review review = reviewRepository.findReviewById(reviewId);

		assertEquals(reviewId, review.getId());
		assertEquals(5, review.getRating());
		assertEquals("Excellent", review.getContents());
	}


}










