package com.study.matzip.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.study.matzip.exception.ForbiddenException;
import com.study.matzip.model.dto.Member;
import com.study.matzip.model.dto.Review;
import com.study.matzip.model.dto.ReviewSearch;
import com.study.matzip.model.service.ReviewService;

import jakarta.servlet.http.HttpSession;
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
	
	@GetMapping("/write")
	String reviewForm(HttpSession session) {
		Member loginUser = (Member) session.getAttribute("loginUser");
		if(loginUser == null) {
			return "redirect:/member/login";
		}
		
		return "review/write";
	}
	
	@PostMapping("/write")
	String writeReview(Review review, HttpSession session, RedirectAttributes redirectAttr){
		Member loginUser = (Member) session.getAttribute("loginUser");
		if(loginUser == null) {
			return "redirect:/member/login";
		}
		
		review.setMno(loginUser.getMno());
		int rno = reviewService.writeReview(review);	
		redirectAttr.addFlashAttribute("msg", "정상적으로 리뷰가 등록 되었습니다.");
		return "redirect:/review/"+rno;
	}
	
	@PostMapping("/{rno}/delete")
	String deleteReview(@PathVariable int rno, HttpSession session,  RedirectAttributes rediAttr) {
		Member loginUser = (Member)session.getAttribute("loginUser");
		if(loginUser == null) {
			return "redirect:/member/login";
		}
		try {
			reviewService.deleteReview(rno, loginUser.getMno());
		} 
		catch (IllegalArgumentException e) {
			rediAttr.addFlashAttribute("msg", e.getMessage());
			return "redirect:/review/list";
		}
		catch (ForbiddenException e) {
			rediAttr.addFlashAttribute("msg", e.getMessage());
			return "redirect:/review/"+rno;
		}
		
		rediAttr.addFlashAttribute("msg", "삭제 완료");
		return "redirect:/review/list";
	}
}