package com.study.matzip.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.study.matzip.exception.InvalidImageException;
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

	// 8-5 정리 결과 (기준: "사용자가 그 자리에서 고칠 수 있나?")
	// - 남김: InvalidImageException (다른 파일을 골라 다시 보내면 됨 → 폼으로)
	// - 삭제 → MvcExceptionHandler: RecordNotFoundException(404), ForbiddenException(403),
	//   UncheckedIOException(사진 저장 실패 → 500, 교육생 판단)
	@GetMapping("/{rno}")
	String selectDetail(@PathVariable int rno, Model model) {
		Review review = reviewService.selectDetail(rno);

		model.addAttribute("review", review);
		log.info("selectDetail 실행");
		return "review/detail";
	}

	@GetMapping("/write")
	String reviewForm() {
		return "review/write";
	}

	@PostMapping("/write")
	String writeReview(Review review, @RequestParam(required = false) MultipartFile image, HttpSession session,
			RedirectAttributes redirectAttr) {
		Member loginUser = (Member) session.getAttribute("loginUser");
		review.setMno(loginUser.getMno());

		int rno; // 초기값을 넣지 않는다 → catch에서 return을 빠뜨리면 컴파일러가 잡아 준다 (확정 대입)
		try {
			rno = reviewService.writeReview(review, image);
		} catch (InvalidImageException e) {
			// 8-5: 사용자가 그 자리에서 고칠 수 있는 입력 오류(다른 파일 선택) → Controller에 남긴다 (튜터 복구)
			// LocalImageStorage가 사용자용 문구를 넣어 두었으므로 그대로 보여 준다
			redirectAttr.addFlashAttribute("msg", e.getMessage());
			return "redirect:/review/write"; // flash는 redirect일 때만 다음 요청으로 전달된다
		}

		redirectAttr.addFlashAttribute("msg", "정상적으로 리뷰가 등록 되었습니다.");
		return "redirect:/review/" + rno;
	}

	@PostMapping("/{rno}/delete")
	String deleteReview(@PathVariable int rno, HttpSession session, RedirectAttributes rediAttr) {
		Member loginUser = (Member) session.getAttribute("loginUser");
		reviewService.deleteReview(rno, loginUser.getMno());

		rediAttr.addFlashAttribute("msg", "삭제 완료");
		return "redirect:/review/list";
	}

	@GetMapping("/{rno}/edit")
	String editForm(@PathVariable int rno, HttpSession session, Model model) {
		Member loginUser = (Member) session.getAttribute("loginUser");

		Review review = reviewService.getReviewForEdit(rno, loginUser.getMno());
		model.addAttribute("review", review);

		return "review/edit";
	}

	@PostMapping("/{rno}/edit")
	String editReview(@PathVariable int rno, Review review, @RequestParam(required = false) MultipartFile image,
			HttpSession session, RedirectAttributes redAttr) {
		Member loginUser = (Member) session.getAttribute("loginUser");
		review.setRno(rno);

		try {
			reviewService.updateReview(loginUser.getMno(), review, image);
		} catch (InvalidImageException e) {
			// 8-5: 사용자가 고칠 수 있는 입력 오류 → 다시 수정 폼으로 (튜터 복구)
			redAttr.addFlashAttribute("msg", e.getMessage());
			return "redirect:/review/{rno}/edit";
		}

		redAttr.addFlashAttribute("msg", "수정 완료");
		return "redirect:/review/{rno}";
	}
}