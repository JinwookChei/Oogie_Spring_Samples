package com.study.matzip.model.storage;

import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

public interface ImageStorage {
	
	// 파일이 없거나 비어 있으면 null을 돌려준다. 
	// 사진 없는 리뷰도 있으니깐. 
	// 이 반환값이 나중에 review.imageName에 들어가요
	String save(MultipartFile image);
}
