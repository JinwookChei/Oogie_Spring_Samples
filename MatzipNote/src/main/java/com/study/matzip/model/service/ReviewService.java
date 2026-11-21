package com.study.matzip.model.service;

import java.util.List;

import com.study.matzip.model.dto.Review;
import com.study.matzip.model.dto.ReviewSearch;

public interface ReviewService {
	
	public List<Review> searchReview(ReviewSearch reviewSearch);
	
	public Review selectDetail(int rno);
	
	public int writeReview(Review review);
	
	public void deleteReview(int rno, int mno);
	
	public void updateReview(int mno, Review review);

	public Review getReviewForEdit(int rno, int mno);
}
