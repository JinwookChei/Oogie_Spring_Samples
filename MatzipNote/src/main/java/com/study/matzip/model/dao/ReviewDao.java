package com.study.matzip.model.dao;

import com.study.matzip.model.dto.Review;

public interface ReviewDao {
	
	public int insert(Review review);
	
	public Review selectByRno(int rno);
	
	public int update(Review review);
	
	public int delete(int rno);
	
	public Review selectDetail(int rno);
}
