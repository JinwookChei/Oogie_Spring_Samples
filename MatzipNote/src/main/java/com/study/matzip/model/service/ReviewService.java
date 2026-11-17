package com.study.matzip.model.service;

import java.util.List;

import com.study.matzip.model.dto.Review;
import com.study.matzip.model.dto.ReviewSearch;

public interface ReviewService {
	
	public List<Review> searchReview(ReviewSearch reviewSearch);
}
