package com.study.matzip.model.storage;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.study.matzip.exception.InvalidImageException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Profile("local")
public class LocalImageStorage implements ImageStorage {

	// 허용할 이미지 확장자 (소문자로 비교)
	private static final List<String> ALLOWED_EXTENSIONS = List.of("png", "jpg", "jpeg", "gif");

	@Value("${app.upload.path}")
	private String imagePath;

	@Override
	public String save(MultipartFile image) {
		if (image == null || image.isEmpty()) {
			return null;
		}

		// 확장자가 없으면 null → 허용 목록 검사에서 함께 걸러진다 ("uuid.null" 방지)
		String exName = StringUtils.getFilenameExtension(image.getOriginalFilename());
		if (exName == null || !ALLOWED_EXTENSIONS.contains(exName.toLowerCase())) {
			throw new InvalidImageException("이미지 파일(png, jpg, jpeg, gif)만 업로드할 수 있습니다.");
		}
		String finalName = UUID.randomUUID() + "." + exName.toLowerCase();

		// IOException(checked)을 던지는 작업은 모두 한 try 안에
		try {
			Path dir = Files.createDirectories(Path.of(imagePath)); // 없으면 만들고, 있으면 그대로
			Path target = dir.resolve(finalName);                   // Path는 불변 → resolve의 결과를 받아야 한다
			image.transferTo(target);                               // 절대 경로에 실제 저장
			log.info("이미지 저장: {}", target);
		} catch (IOException e) {
			// checked 예외는 @Transactional이 rollback하지 않으므로 unchecked로 바꿔 던진다 (원인 e를 함께 넘김)
			throw new UncheckedIOException("이미지 저장 실패: " + finalName, e);
		}
		
		return finalName; // DB의 review.image_name에 들어갈 저장명
	}
}
