package com.study.matzip.model.service;

import java.io.UncheckedIOException;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.study.matzip.exception.ForbiddenException;
import com.study.matzip.exception.RecordNotFoundException;
import com.study.matzip.model.dao.ReviewDao;
import com.study.matzip.model.dto.Review;
import com.study.matzip.model.dto.ReviewSearch;
import com.study.matzip.model.storage.ImageStorage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

	private final ReviewDao reviewDao;
	private final ImageStorage imageStorage;
	
	@Override
	public List<Review> searchReview(ReviewSearch reviewSearch) {
		return reviewDao.search(reviewSearch);
	}

	@Override
	public Review selectDetail(int rno) {
		Review review = reviewDao.selectDetail(rno);
		if (review == null) {
			// TODO: 8-4-2. "없음"을 뜻하는 커스텀 예외(RecordNotFoundException)로 바꾸기 (아래 checkReviewMno도)
			throw new RecordNotFoundException("찾을 수 없는 리뷰 입니다.");
		}
		return review;
	}
	
	@Override
	public int writeReview(Review review, MultipartFile image) {
		String imageName = imageStorage.save(image);
		review.setImageName(imageName);
		reviewDao.insert(review);
		return review.getRno();
	}
	
	@Override
	public void deleteReview(int rno, int mno) {
		checkReviewMno(rno, mno);
		reviewDao.delete(rno);
	}
	
	@Override
	public void updateReview(int mno, Review review, MultipartFile image) {
		// 작성자 검사용으로 조회한 DB 원본을 버리지 않고 받아 둔다 (원래 사진 이름이 들어 있음)
		Review original = checkReviewMno(review.getRno(), mno);

		// 새 사진이 없으면 save()가 null → 원래 사진 유지 / 있으면 새 저장명으로 교체
		// (폼에서 hidden으로 원래 이름을 받지 않는다: 사용자가 바꿔서 남의 파일 이름을 넣을 수 있음)
		String newImageName = imageStorage.save(image);
		review.setImageName(newImageName != null ? newImageName : original.getImageName());

		reviewDao.update(review);
	}

	@Override
	public Review getReviewForEdit(int rno, int mno) {
		return checkReviewMno(rno, mno);
	}

	private Review checkReviewMno(int rno, int mno) {
		Review selectedReview = reviewDao.selectByRno(rno);
		if(selectedReview == null) {
			// TODO: 8-4-2. 여기도 RecordNotFoundException으로
			throw new RecordNotFoundException("찾을 수 없는 리뷰 입니다.");
		}		
		if(mno != selectedReview.getMno()) {
			throw new ForbiddenException("작성자가 아니어서 접근 할 수 없습니다.");
		}
		return selectedReview;
	}
}
