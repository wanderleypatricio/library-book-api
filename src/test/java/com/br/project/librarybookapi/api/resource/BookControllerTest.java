package com.br.project.librarybookapi.api.resource;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.Optional;

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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MockMvcBuilder;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.br.project.librarybookapi.dto.BookDTO;
import com.br.project.librarybookapi.exception.BusinessException;
import com.br.project.librarybookapi.model.Book;
import com.br.project.librarybookapi.resource.BookController;
import com.br.project.librarybookapi.service.BookService;
import com.br.project.librarybookapi.service.LoanService;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest(controllers = BookController.class)
@AutoConfigureMockMvc
public class BookControllerTest {

	static String BOOK_API = "/api/books";
	@Autowired
	MockMvc mvc;
	
	@MockBean
	BookService bookService;
	
	@MockBean
	LoanService loanService;
	
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
	
	
	@Test
	@DisplayName("deve obter informações do livro")
	public void getBookDetailsTest() throws Exception{
		Long id = (long) 123;
		
		Book book = Book.builder()
				.id(id)
				.title(createNewBook().getTitle())
				.author(createNewBook().getAuthor())
				.isbn(createNewBook().getIsbn())
				.build();
		
		BDDMockito.given(bookService.getById(id)).willReturn(Optional.of(book));
		
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
				.get(BOOK_API.concat("/"+id))
				.accept(MediaType.APPLICATION_JSON);
		
		mvc.perform(request)
		.andExpect(status().isOk())
		.andExpect(jsonPath("id").value(id))
		.andExpect(jsonPath("title").value(createNewBook().getTitle()))
		.andExpect(jsonPath("author").value(createNewBook().getAuthor()))
		.andExpect(jsonPath("isbn").value(createNewBook().getIsbn()));
	}
	
	
	@Test
	@DisplayName("deve retornar resource not found quando o livro procurado não existe")
	public void BookNotFoundTest() throws Exception{
		
		BDDMockito.given(bookService.getById(Mockito.anyLong())).willReturn(Optional.empty());
		
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
				.get(BOOK_API.concat("/"+1))
				.accept(MediaType.APPLICATION_JSON);
		
		mvc.perform(request)
		.andExpect(status().isNotFound());
	}
	
	@Test
	@DisplayName("Deve deletar um livro")
	public void deleteBookTest() throws Exception{
		BDDMockito.given(bookService.getById(anyLong())).willReturn(Optional.of(Book.builder().id((long) 11).build()));
	
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete(BOOK_API.concat("/"+1));
		
		mvc.perform(request)
		.andExpect(status().isNoContent());
	}
	
	@Test
	@DisplayName("Deve retornar notfound quando não conseguir deletar um livro")
	public void notFoundDeleteBookTest() throws Exception{
		BDDMockito.given(bookService.getById(anyLong())).willReturn(Optional.empty());
	
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete(BOOK_API.concat("/"+1));
		
		mvc.perform(request)
		.andExpect(status().isNotFound());
	}
	
	@Test
	@DisplayName("Deve atualizar um livro")
	public void updateBookTest() throws Exception{
		Long id = (long) 11;
		
		String json = new ObjectMapper().writeValueAsString(createNewBook());
		
		Book book = Book.builder()
				.id((long) 11)
				.title("As tranças do rei careca")
				.author("José de Abreu")
				.isbn("321")
				.build();
		
		BDDMockito.given(bookService.getById(id)).willReturn(Optional.of(book));
		
		Book updatedBook = Book.builder().id(id).author("Artur").title("A volta dos que não foram").build();
		
		BDDMockito.given(bookService.update(book)).willReturn(updatedBook);
		
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
				.put(BOOK_API.concat("/"+1))
				.content(json)
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON);
		
		mvc.perform(request)
		.andExpect(status().isOk())
		.andExpect(jsonPath("id").value(id))
		.andExpect(jsonPath("title").value(createNewBook().getTitle()))
		.andExpect(jsonPath("author").value(createNewBook().getAuthor()))
		.andExpect(jsonPath("isbn").value("321"));
		
	}
	
	@Test
	@DisplayName("Deve retornar notfound quando não conseguir atualizar um livro")
	public void updateInexistentBookTest() throws Exception{
		
		
		String json = new ObjectMapper().writeValueAsString(createNewBook());
		
		BDDMockito.given(bookService.getById(Mockito.anyLong())).willReturn(Optional.empty());
		
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
				.put(BOOK_API.concat("/"+1))
				.content(json)
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON);
		
		mvc.perform(request)
		.andExpect(status().isNotFound());
	}
	
	@Test
	@DisplayName("deve filtrar os livros")
	public void findBookTest() throws Exception{
		Long id = (long) 11;
		Book book = Book.builder()
				.id(id)
				.title("As tranças do rei careca")
				.author("José de Abreu")
				.isbn("321")
				.build();
		
		BDDMockito.given(bookService.find(Mockito.any(Book.class), Mockito.any(Pageable.class)))
		.willReturn(new PageImpl<Book>(Arrays.asList(book), PageRequest.of(0, 100), 1));
		
		String queryString = String.format("?title=%s&author=%s&page=0&size=100", book.getTitle(), book.getAuthor());
		
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
				.get(BOOK_API.concat(queryString))
				.accept(MediaType.APPLICATION_JSON);
		
		mvc.perform(request)
		.andExpect(status().isOk())
		.andExpect(jsonPath("content", Matchers.hasSize(1)))
		.andExpect(jsonPath("totalElements").value(1))
		.andExpect(jsonPath("pageable.pageSize").value(100))
		.andExpect(jsonPath("pageable.pageNumber").value(0));
	}
	
	private static BookDTO createNewBook() {
		return BookDTO.builder().author("Artur").title("A volta dos que não foram").isbn("123").build();
	}
}
