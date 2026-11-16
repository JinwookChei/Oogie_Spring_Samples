package com.study.matzip;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.study.matzip.model.dao.ReviewDao;
import com.study.matzip.model.dto.Review;
import com.study.matzip.model.dto.ReviewSearch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;



@RequiredArgsConstructor
@Slf4j
@SpringBootTest
public class ReviewDaoTest {
	@Autowired
	private ReviewDao reviewDao;
	
	@Test
	public void searchTest() {
	    assertNotNull(reviewDao);
	    
	    ReviewSearch rs = ReviewSearch.builder()
	            .keyword("너무")
	            .minRating(1).build();
	    
	    List<Review> searchedReviews = reviewDao.search(rs);
	    for(int r = 0; r < searchedReviews.size(); ++r) {
	        Review review = searchedReviews.get(r);
	        log.debug("{}번째 - Content: {}, Rating: {}", r + 1, review.getContent(), review.getRating());
	    }
	}
}
