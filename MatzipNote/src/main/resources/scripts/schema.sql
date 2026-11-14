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

-- 4. 테스트 데이터 (부모 -> 자식 순서)
-- 위에서 테이블을 새로 만들었으므로 AUTO_INCREMENT는 1부터 시작 -> 첫 회원의 mno = 1
INSERT INTO member (email, password, name)
VALUES ('kim@matzip.com', '1234', '김맛집');

INSERT INTO review (mno, title, content, rating, image_name)
VALUES (1, '을지로 노포 냉면', '육수가 진하고 면이 쫄깃하다. 웨이팅 30분은 각오할 것.', 5, 'sample-naengmyeon.jpg');

INSERT INTO review (mno, title, content, rating)
VALUES (1, '회사 앞 김밥집', '무난하다. 바쁠 때 빨리 먹기 좋음.', 3);
