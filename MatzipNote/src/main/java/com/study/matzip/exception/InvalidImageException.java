package com.study.matzip.exception;

// 업로드한 파일이 허용된 이미지가 아닐 때.
// IllegalArgumentException을 상속 → "잘못된 입력"이라는 의미는 유지하면서,
// Controller가 "찾을 수 없는 리뷰"(IllegalArgumentException)와 구분해서 잡을 수 있다.
public class InvalidImageException extends IllegalArgumentException {

	public InvalidImageException(String message) {
		super(message);
	}
}
