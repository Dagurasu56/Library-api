package com.dagurasu.libraryapi.api.dto;

import javax.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookDTO {

	private Long id;

	@NotEmpty
	private String title;

	@NotEmpty
	private String author;

	@NotEmpty
	private String isbn;

}
