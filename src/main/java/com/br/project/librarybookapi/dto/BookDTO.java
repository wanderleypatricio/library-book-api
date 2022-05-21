package com.br.project.librarybookapi.dto;


import javax.validation.constraints.NotEmpty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookDTO {
	@NotEmpty
	private String title;
	@NotEmpty
	private String author;
	@NotEmpty
	private String isbn;
	private Long id;
		
}
