package com.study.matzip.model.dao;

import java.util.List;

import com.study.matzip.model.dto.Review;
import com.study.matzip.model.dto.ReviewSearch;

public interface ReviewDao {
	
	public int insert(Review review);
	
	public Review selectByRno(int rno);
	
	public int update(Review review);
	
	public int delete(int rno);
	
	public Review selectDetail(int rno);
	
	public List<Review> search(ReviewSearch reviewSearch);
}
