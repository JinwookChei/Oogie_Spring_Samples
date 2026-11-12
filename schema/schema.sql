CREATE DATABASE IF NOT EXISTS matzip;
USE matzip;

-- 1. 외래 키로 연결되어 있으므로 자식 테이블을 먼저 삭제합니다.
DROP TABLE IF EXISTS review;
DROP TABLE IF EXISTS member;

-- 2. 부모 테이블 생성
CREATE TABLE member (
    mno INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(40) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    name VARCHAR(10) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 3. 자식 테이블 생성
CREATE TABLE review (
    rno INT AUTO_INCREMENT PRIMARY KEY,
    mno INT NOT NULL,
    title VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    rating INT NOT NULL CHECK(1 <= rating AND rating <= 5),
    image_name VARCHAR(50),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (mno) REFERENCES member(mno)
);