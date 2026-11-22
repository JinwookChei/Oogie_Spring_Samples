package com.study.matzip.model.storage;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@Profile("fake")
public class FakeImageStorage implements ImageStorage {

	@Override
	public String save(MultipartFile image) {
		if(image == null || image.isEmpty()) {
			return null;
		}
		String fileName = image.getOriginalFilename();
		log.info("{} 저장 하지 않음", fileName);
		
		return "fake-"+fileName;
	}
}
