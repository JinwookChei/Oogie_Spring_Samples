package com.study.matzip.model.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.study.matzip.exception.ForbiddenException;
import com.study.matzip.model.dao.ReviewDao;
import com.study.matzip.model.dto.Review;
import com.study.matzip.model.dto.ReviewSearch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

	private final ReviewDao reviewDao;

	@Override
	public List<Review> searchReview(ReviewSearch reviewSearch) {
		return reviewDao.search(reviewSearch);
	}

	@Override
	public Review selectDetail(int rno) {
		Review review = reviewDao.selectDetail(rno);
		if (review == null) {
			throw new IllegalArgumentException("찾을 수 없는 리뷰 입니다.");
		}
		return review;
	}
	
	@Override
	public int writeReview(Review review) {
		reviewDao.insert(review);
		return review.getRno();
	}
	
	@Override
	public void deleteReview(int rno, int mno) {
		checkReviewMno(rno, mno);
		reviewDao.delete(rno);
	}
	
	@Override
	public void updateReview(int mno, Review review) {
		checkReviewMno(review.getRno(), mno);
		reviewDao.update(review);
	}
	
	private Review checkReviewMno(int rno, int mno) {
		Review selectedReview = reviewDao.selectByRno(rno);
		if(selectedReview == null) {
			throw new IllegalArgumentException("찾을 수 없는 리뷰 입니다.");
		}		
		if(mno != selectedReview.getMno()) {
			throw new ForbiddenException("작성자가 아니어서 접근 할 수 없습니다.");
		}
		return selectedReview;
	}
}
