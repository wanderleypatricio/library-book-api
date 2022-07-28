package com.br.project.librarybookapi.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanDTO {
	private Long id;
	private String isbn;
	private String customer;
	private String email;
	private LocalDate loanDate;
	private BookDTO book;
}
