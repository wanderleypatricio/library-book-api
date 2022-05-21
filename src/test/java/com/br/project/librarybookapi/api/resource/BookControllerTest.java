package com.br.project.librarybookapi.api.resource;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.br.project.librarybookapi.dto.BookDTO;
import com.br.project.librarybookapi.exception.BusinessException;
import com.br.project.librarybookapi.model.Book;
import com.br.project.librarybookapi.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest
@AutoConfigureMockMvc
public class BookControllerTest {

	static String BOOK_API = "/api/books";
	@Autowired
	MockMvc mvc;
	
	@MockBean
	BookService bookService;	
	
	@Test
	@DisplayName("Deve criar um livro com sucesso.")
	public void createBookTest()  throws Exception{
		
		BookDTO dto = createNewBook();
		BDDMockito.given(bookService.save(Mockito.any(Book.class)))
		.willReturn(com.br.project.librarybookapi.model.Book.builder().id((long) 1).author("Artur").title("A volta dos que não foram").isbn("123").build());
		String json = new ObjectMapper().writeValueAsString(dto);
		
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(BOOK_API)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(json);
		
		mvc.perform(request)
		.andExpect(status().isCreated())
		.andExpect(jsonPath("id").isEmpty())
		.andExpect(jsonPath("title").value("A volta dos que não foram"))
		.andExpect(jsonPath("author").value("Artur"))
		.andExpect(jsonPath("isbn").value("123"));
		
	}
	
	@Test
	@DisplayName("Deve lançar erro de validação quando não houver dados suficientes para validação do livro")
	public void createInvalidBookTest() throws Exception {
		String json = new ObjectMapper().writeValueAsString(new BookDTO());
		
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(BOOK_API)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(json);
		
		mvc.perform(request)
		.andExpect(status().isBadRequest())
		.andExpect(jsonPath("errors", Matchers.hasSize(3)));
	}
	
	@Test
	@DisplayName("Deve lançar ao tentar cadastrar livro com isbn já existente")
	public void createBookWithDuplicatedIsbnTest() throws Exception {
		
		BookDTO dto = createNewBook();
		
		String json = new ObjectMapper().writeValueAsString(dto);
		BDDMockito.given(bookService.save(Mockito.any(Book.class)))
		.willThrow(new BusinessException("Isbn já cadastrado"));
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(BOOK_API)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(json);
		
		mvc.perform(request)
		.andExpect(status().isBadRequest())
		.andExpect(jsonPath("errors", Matchers.hasSize(1)))
		.andExpect(jsonPath("errors[0]").value("Isbn já cadastrado"));
	}
	
	
	private BookDTO createNewBook() {
		return BookDTO.builder().author("Artur").title("A volta dos que não foram").isbn("123").build();
	}
}
