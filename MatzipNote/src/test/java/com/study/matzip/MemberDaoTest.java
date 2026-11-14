package com.study.matzip;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.study.matzip.model.dao.MemberDao;
import com.study.matzip.model.dto.Member;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
@Transactional
public class MemberDaoTest {
	
	@Autowired
	MemberDao memberDao;
	
	@Test
	public void insertTest() {
		assertNotNull(memberDao);
		
		Member m1 = new Member().builder()
				.email("test1@test.com")
				.password("1111")
				.name("테스트1")
				.build();
		
		Member m2 = new Member().builder()
				.email("test2@test.com")
				.password("1111")
				.name("테스트2")
				.build();
		
		int res1 = memberDao.insert(m1);
		int res2 = memberDao.insert(m2);
		log.debug("insertTest: {}", m1.getMno());
		log.debug("insertTest: {}", m2.getMno());
		
		assertEquals(1, res1);
		assertEquals(1, res2);
		assertNotEquals(0, m1.getMno());
		assertNotEquals(0, m2.getMno());
	}
	
	@Test
	public void selectByEmailTest() {
		assertNotNull(memberDao);
		Member m1 = memberDao.selectByEmail("kim@matzip.com");
		assertEquals(1, m1.getMno());
		log.debug("selectByEmailTest: {}", m1.getMno());
		
		Member m2 = memberDao.selectByEmail("test@matzip.com");
		assertNull(m2);
	}
}
