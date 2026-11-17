package com.study.matzip.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.study.matzip.model.dto.Review;
import com.study.matzip.model.dto.ReviewSearch;
import com.study.matzip.model.service.ReviewService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/review")
@RequiredArgsConstructor
public class ReviewController {

	private final ReviewService reviewService;

	@GetMapping("/list")
	String searchReview(@ModelAttribute ReviewSearch reviewSearch, Model model) {
		List<Review> reviewList = reviewService.searchReview(reviewSearch);
		model.addAttribute("reviewList", reviewList);

		log.info("searchReview 실행");
		return "review/list";
	}

	@GetMapping("/{rno}")
	String selectDetail(@PathVariable int rno, Model model) {
		Review review = null;
		try {
			review = reviewService.selectDetail(rno);
		} catch (IllegalArgumentException e) {
			model.addAttribute("error", e.getMessage());
			return "review/detail";
		}

		model.addAttribute("review", review);
		log.info("selectDetail 실행");
		return "review/detail";
	}
}