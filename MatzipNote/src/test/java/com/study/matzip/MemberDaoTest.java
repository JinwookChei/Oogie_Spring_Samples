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
		
		Member m1 = Member.builder()
				.email("test1@test.com")
				.password("1111")
				.name("테스트1")
				.build();
		
		Member m2 = Member.builder()
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

		// Given: DB에 이미 있는 데이터에 기대지 않고, 이 테스트가 직접 회원을 넣는다 (@Transactional → 끝나면 rollback)
		Member saved = Member.builder()
				.email("select-test@matzip-test.com")
				.password("1111")
				.name("조회테스트")
				.build();
		memberDao.insert(saved);   // useGeneratedKeys → saved.mno에 DB가 만든 번호가 채워짐

		// When: 방금 넣은 이메일로 조회
		Member found = memberDao.selectByEmail("select-test@matzip-test.com");
		log.debug("selectByEmailTest: {}", found);

		// Then: AUTO_INCREMENT 번호는 예측할 수 없으므로 숫자 1이 아니라 insert 후 채워진 mno와 비교
		assertNotNull(found);
		assertEquals(saved.getMno(), found.getMno());
		assertEquals(saved.getEmail(), found.getEmail());
		assertEquals(saved.getName(), found.getName());

		// 없는 이메일 → null (실제로 가입할 일 없는 주소)
		Member notFound = memberDao.selectByEmail("no-such-user@matzip-test.com");
		assertNull(notFound);
	}
}
