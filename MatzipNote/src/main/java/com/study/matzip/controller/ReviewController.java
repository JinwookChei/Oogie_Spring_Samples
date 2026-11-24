package com.study.matzip.controller;

import java.io.UncheckedIOException;
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

import com.study.matzip.exception.ForbiddenException;
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

	// TODO: 8-5. 이 Controller의 try-catch를 하나씩 보고 "여기 남길 것 / Advice로 보낼 것"을 정해서 정리한다.
	//            판단 기준은 교안 8-5 표. 특히 ForbiddenException catch 2곳(editForm, editReview)은
	//            M5 셀프 체크 Q1("Advice가 있으면 이 catch는?")의 답을 직접 확인하는 곳이다.
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
		} catch (UncheckedIOException e) {
			// 디스크 저장 실패: 메시지(저장명 등)는 내부 정보 → 서버 로그에만, 사용자에겐 일반 문구
			log.error("리뷰 사진 저장 실패", e);
			redirectAttr.addFlashAttribute("msg", "사진 저장 중 오류가 발생했습니다. 다시 시도해 주세요.");
			return "redirect:/review/write";
		} catch (InvalidImageException e) {
			// 이미지가 아닌 파일: LocalImageStorage가 사용자용 문구를 넣어 두었으므로 그대로 보여 준다
			redirectAttr.addFlashAttribute("msg", e.getMessage());
			return "redirect:/review/write"; // flash는 redirect일 때만 다음 요청으로 전달된다
		}

		redirectAttr.addFlashAttribute("msg", "정상적으로 리뷰가 등록 되었습니다.");
		return "redirect:/review/" + rno;
	}

	@PostMapping("/{rno}/delete")
	String deleteReview(@PathVariable int rno, HttpSession session, RedirectAttributes rediAttr) {
		Member loginUser = (Member) session.getAttribute("loginUser");
		try {
			reviewService.deleteReview(rno, loginUser.getMno());
		} catch (IllegalArgumentException e) {
			rediAttr.addFlashAttribute("msg", e.getMessage());
			return "redirect:/review/list";
		}
		// ForbiddenException(남의 리뷰)은 여기서 잡지 않는다
		// → 메서드 밖으로 던져짐 → MvcExceptionHandler.handleForbidden() → error/403.jsp (상태 코드
		// 403)

		rediAttr.addFlashAttribute("msg", "삭제 완료");
		return "redirect:/review/list";
	}

	@GetMapping("/{rno}/edit")
	String editForm(@PathVariable int rno, HttpSession session, Model model, RedirectAttributes rediAttr) {
		Member loginUser = (Member) session.getAttribute("loginUser");

		try {
			Review review = reviewService.getReviewForEdit(rno, loginUser.getMno());
			model.addAttribute("review", review);
		} catch (IllegalArgumentException e) {
			rediAttr.addFlashAttribute("msg", e.getMessage());
			return "redirect:/review/list";
		} catch (ForbiddenException e) {
			rediAttr.addFlashAttribute("msg", e.getMessage());
			return "redirect:/review/" + rno;
		}

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
			// ⚠️ 하위 타입을 먼저 잡아야 한다 (아래 IllegalArgumentException보다 위에 있어야 함)
			// 이미지가 아닌 파일 → 다시 수정 폼으로
			redAttr.addFlashAttribute("msg", e.getMessage());
			return "redirect:/review/{rno}/edit";
		} catch (UncheckedIOException e) {
			log.error("리뷰 사진 저장 실패 (수정)", e);
			redAttr.addFlashAttribute("msg", "사진 저장 중 오류가 발생했습니다. 다시 시도해 주세요.");
			return "redirect:/review/{rno}/edit";
		} catch (IllegalArgumentException e) {
			// 찾을 수 없는 리뷰 → 목록으로
			redAttr.addFlashAttribute("msg", e.getMessage());
			return "redirect:/review/list";
		} catch (ForbiddenException e) {
			redAttr.addFlashAttribute("msg", e.getMessage());
			return "redirect:/review/" + rno;
		}
		redAttr.addFlashAttribute("msg", "수정 완료");
		return "redirect:/review/{rno}";
	}
}