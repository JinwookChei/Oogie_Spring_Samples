package com.study.matzip.model.dao;

import com.study.matzip.model.dto.Review;

public interface ReviewDao {
	
	public int insert(Review review);
	
	public Review seletByRno(int rno);
	
	public int update(int rno, Review review);
	
	public int delete(int rno);
}
