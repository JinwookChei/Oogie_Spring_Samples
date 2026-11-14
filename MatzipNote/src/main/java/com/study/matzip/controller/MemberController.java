package com.study.matzip.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.study.matzip.model.dto.Member;
import com.study.matzip.model.service.MemberService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {

	private final MemberService ms;

	@GetMapping("/signUp-form")
	public String signupForm() {
		return "member/signup";
	}

	@PostMapping("/signup")
	public String signUp(@ModelAttribute Member member, Model model) {
		try {
			ms.signUp(member);
		} catch (IllegalArgumentException e) {
			log.debug("회원가입 실패: {}", e.getMessage());
			model.addAttribute("error", e.getMessage());
			return "member/signup";
		}

		return "redirect:/";
	}
}
