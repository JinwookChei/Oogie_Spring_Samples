package com.study.matzip;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import com.study.matzip.model.storage.FakeImageStorage;
import com.study.matzip.model.storage.ImageStorage;

import lombok.extern.slf4j.Slf4j;

// application.properties의 spring.profiles.active=local 보다 @ActiveProfiles가 우선한다
// → 이 테스트에서만 fake 프로파일 → FakeImageStorage만 빈으로 등록된다 (디스크에 파일이 생기지 않음)
@Slf4j
@SpringBootTest
@ActiveProfiles("fake")
public class ImageStorageTest {

	// 구현체가 아니라 인터페이스 타입으로 주입받는다 (ReviewServiceImpl과 같은 조건)
	@Autowired
	ImageStorage imageStorage;

	// 1. 어떤 구현체가 주입됐는지: 프로파일이 fake면 FakeImageStorage여야 한다
	@Test
	void injected_isFake() {
		log.info("주입된 ImageStorage: {}", imageStorage.getClass().getName());
		assertInstanceOf(FakeImageStorage.class, imageStorage);
	}

	// 2. 파일이 있으면 저장하지 않고 "fake-원래이름"을 돌려준다
	@Test
	void save_returnsFakeName() {
		// MockMultipartFile(파라미터 이름, 원래 파일명, contentType, 내용)
		MockMultipartFile image = new MockMultipartFile("image", "cat.png", "image/png", "가짜 이미지".getBytes());

		String savedName = imageStorage.save(image);

		assertEquals("fake-cat.png", savedName);
	}

	// 3. 빈 파일(내용 0바이트) → isEmpty() == true → 인터페이스 약속대로 null
	@Test
	void save_emptyFile_returnsNull() {
		MockMultipartFile empty = new MockMultipartFile("image", "cat.png", "image/png", new byte[0]);

		assertNull(imageStorage.save(empty));
	}

	// 4. null(파일 파트 자체가 없음)도 null
	@Test
	void save_null_returnsNull() {
		assertNull(imageStorage.save(null));
	}
}
