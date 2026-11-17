package com.study.matzip.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.study.matzip.model.dto.Member;
import com.study.matzip.model.service.MemberService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {

	private final MemberService ms;

	@GetMapping("/signup")
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

	@GetMapping("/login")
	public String loginForm() {
		return "member/login";
	}

	@PostMapping("/login")
	public String login(@RequestParam String email, @RequestParam String password, HttpSession session, Model model) {
		Member member;
		try {
			member = ms.login(email, password);
		} catch (IllegalArgumentException e) {
			log.debug("로그인 실패: {}", e.getMessage());
			model.addAttribute("error", e.getMessage());
			return "member/login";
		}

		session.setAttribute("loginUser", member);
		return "redirect:/";
	}

	@PostMapping("/logout")
	public String logout(HttpSession session) {
		session.invalidate();
		return "redirect:/";
	}
}
