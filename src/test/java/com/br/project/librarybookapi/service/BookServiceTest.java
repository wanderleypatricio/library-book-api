package com.br.project.librarybookapi.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.br.project.librarybookapi.exception.BusinessException;
import com.br.project.librarybookapi.model.Book;
import com.br.project.librarybookapi.repository.BookRepository;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class BookServiceTest {
	BookService service;
	
	@MockBean
	BookRepository repository;
	
	@BeforeEach
	public void setUp() {
		this.service = new BookServiceImpl(repository);
	}
	
	@Test
	@DisplayName("Deve salvar o livro")
	public void saveBookTest() {
		Book book = createValidBook();	
		Mockito.when(repository.existsByIsbn(Mockito.anyString())).thenReturn(false);
		Mockito.when(repository.save(book)).thenReturn(
				Book.builder()
				.id((long) 123)
				.author("Wanderley")
				.title("Pueira em alto mar")
				.isbn("123")
				.build());
		Book saveBook = service.save(book);
		
		assertThat(saveBook.getId()).isNotNull();
		assertThat(saveBook.getTitle()).isEqualTo("Pueira em alto mar");
		assertThat(saveBook.getAuthor()).isEqualTo("Wanderley");
		assertThat(saveBook.getIsbn()).isEqualTo("123");
	}
	
	private Book createValidBook() {
		return Book.builder().author("Wanderley").title("Pueira em alto mar").isbn("123").build();
	}
	
	@Test
	@DisplayName("lança uma excessão caso já tenho livro com isbn cadastrado")
	public void shouldNotSaveABookWithDuplicatedIsbn() {
		Book book = createValidBook();
		Mockito.when(repository.existsByIsbn(Mockito.anyString())).thenReturn(true);
		Throwable exception = Assertions.catchThrowable(() -> service.save(book));
		assertThat(exception)
		.isInstanceOf(BusinessException.class)
		.hasMessage("Isbn já cadastrado");
		
		Mockito.verify(repository, Mockito.never()).save(book);
	}
}
