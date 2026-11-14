package com.study.matzip.model.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Member {
	private int mno;
	private String email;
	private String password;
	private String name;
	private LocalDateTime createdAt; 
}
