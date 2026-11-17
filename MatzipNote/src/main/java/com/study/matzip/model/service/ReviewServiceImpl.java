package com.study.matzip.model.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.study.matzip.model.dao.ReviewDao;
import com.study.matzip.model.dto.Review;
import com.study.matzip.model.dto.ReviewSearch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService{
	
	private final ReviewDao reviewDao;
	
	@Override
	public List<Review> searchReview(ReviewSearch reviewSearch){
		return reviewDao.search(reviewSearch);
	}
}
