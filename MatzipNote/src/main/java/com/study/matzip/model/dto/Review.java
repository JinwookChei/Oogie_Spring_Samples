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
public class Review {
	private int rno;
	private int mno;
	private String title;
	private String content;
	private int rating;
	private String imageName;
	private LocalDateTime createdAt;
}
