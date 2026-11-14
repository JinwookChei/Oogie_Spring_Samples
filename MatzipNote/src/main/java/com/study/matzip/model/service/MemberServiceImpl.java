package com.study.matzip.model.service;

import org.springframework.stereotype.Service;

import com.study.matzip.model.dao.MemberDao;
import com.study.matzip.model.dto.Member;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

	private final MemberDao memberDao;

	@Override
	public int signUp(Member member) {

		// 1. 중복 검사 (guard clause: 안 되는 경우를 먼저 막는다)
		// DB에도 email UNIQUE가 있지만, 여기서 먼저 막는 이유:
		// - DB 에러(DuplicateKeyException, "Duplicate entry...")는 사용자에게 보여 줄 수 없는 메시지
		// - Service = 업무 규칙("이메일은 하나만")이 사는 곳, DB 제약 = 마지막 방어선
		if (memberDao.selectByEmail(member.getEmail()) != null) {
			// 로그만 찍으면 아래 insert가 그대로 실행된다 → throw로 흐름을 멈춘다
			// RuntimeException 계열이라 throws 선언 불필요 (커스텀 예외는 M3에서)
			throw new IllegalArgumentException("이미 가입된 이메일입니다: " + member.getEmail());
		}

		// 2. 등록
		// 반환값(영향받은 행 수) 검사는 하지 않는다:
		// insert가 실패하면 0이 아니라 예외(DataAccessException)가 나기 때문
		memberDao.insert(member);

		// 3. useGeneratedKeys + keyProperty="mno" 덕분에 insert 후 member.mno가 채워져 있다
		// 비밀번호 같은 민감 정보는 로그에 남기지 않는다 → mno, email만
		log.info("회원 가입 완료: mno={}, email={}", member.getMno(), member.getEmail());
		return member.getMno();
	}
}
