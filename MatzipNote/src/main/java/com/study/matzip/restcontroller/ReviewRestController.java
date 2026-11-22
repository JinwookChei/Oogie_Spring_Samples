package com.study.matzip.restcontroller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.study.matzip.model.dto.Member;
import com.study.matzip.model.dto.Review;
import com.study.matzip.model.service.ReviewService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
public class ReviewRestController {
	
	private final ReviewService reviewService;
	
	@GetMapping("/{rno}")
	public ResponseEntity<Review> detail(@PathVariable int rno){
		Review review = reviewService.selectDetail(rno);
		return ResponseEntity.ok(review);
	}
	
	@PostMapping
	public ResponseEntity<Review> write(@RequestBody Review review, HttpSession session){
		Member loginUser = (Member)session.getAttribute("loginUser");
		if(loginUser == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		
		review.setMno(loginUser.getMno());
		reviewService.writeReview(review, null);
		return ResponseEntity.status(HttpStatus.CREATED).body(review);
	}
}
