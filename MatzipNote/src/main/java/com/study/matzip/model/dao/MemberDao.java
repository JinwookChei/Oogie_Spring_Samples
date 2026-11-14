package com.study.matzip.model.dao;

import com.study.matzip.model.dto.Member;

public interface MemberDao {
	
	public int insert(Member member);
	
	public Member selectByEmail(String email); 
}
