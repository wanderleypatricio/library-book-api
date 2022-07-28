package com.br.project.librarybookapi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.br.project.librarybookapi.exception.BusinessException;
import com.br.project.librarybookapi.model.Book;
import com.br.project.librarybookapi.repository.BookRepository;
import com.br.project.librarybookapi.service.impl.BookServiceImpl;

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
				.id((long) 11)
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
	
	@Test
	@DisplayName("deve retonar o livro por Id")
	public void getByIdTest() {
		Long id = (long) 11;
		Book book = createValidBook();
		
		Mockito.when(repository.findById(id)).thenReturn(Optional.of(book));
		
		Optional<Book> foundBook = service.getById(id);
		
		assertThat(foundBook.isPresent()).isTrue();
		assertThat(foundBook.get().getId()).isEqualTo(id);
		assertThat(foundBook.get().getAuthor()).isEqualTo(book.getAuthor());
		assertThat(foundBook.get().getTitle()).isEqualTo(book.getTitle());
		assertThat(foundBook.get().getIsbn()).isEqualTo(book.getIsbn());
	}
	
	@Test
	@DisplayName("deve retonar vazio ao obter o livro por Id quando ele não existe na base")
	public void bookNotFoundByIdTest() {
		Long id = (long) 11;
		
		Mockito.when(repository.findById(id)).thenReturn(Optional.empty());
		
		Optional<Book> foundBook = service.getById(id);
		
		assertThat(foundBook.isPresent()).isFalse();
	}
	
	@Test
	@DisplayName("deve deletar livro por Id")
	public void bookDeleteTest() {
		Long id = (long) 11;
		
		Book book = Book.builder().id(id).build();
		
		org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> service.delete(book));
		
		Mockito.verify(repository, Mockito.times(1)).delete(book);
	}
	
	@Test
	@DisplayName("deve ocorrer erro ao tentar deletar livro inexistente")
	public void DeleteInvalidBookTest() {
		Book book = new Book();
		
		org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> service.delete(book));
		
		Mockito.verify(repository, Mockito.never()).delete(book);
	}
	
	@Test
	@DisplayName("deve ocorrer erro ao tentar atualizar livro inexistente")
	public void updateInvalidBookTest() {
		Book book = new Book();
		
		org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> service.update(book));
		
		Mockito.verify(repository, Mockito.never()).save(book);
	}
	
	@Test
	@DisplayName("deve atualizar livro")
	public void updateBookTest() {
		Long id = (long) 11;
		Book updatingbook = Book.builder().id(id).build();
		
		Book updatedBook = createValidBook();
		updatedBook.setId(id);

		Mockito.when(repository.save(updatingbook)).thenReturn(updatedBook);
		
		Book book = service.save(updatingbook);
		
		assertThat(book.getId()).isEqualTo(updatedBook.getId());
		assertThat(book.getAuthor()).isEqualTo(updatedBook.getAuthor());
		assertThat(book.getIsbn()).isEqualTo(updatedBook.getIsbn());
		assertThat(book.getTitle()).isEqualTo(updatedBook.getTitle());
	}
	
	@Test
	@DisplayName("deve filtrar os livros pelas propriedades")
	public void findBookTest() {
		
		Book book = createValidBook();

		PageRequest pageRequest  = PageRequest.of(0, 10);
		
		List<Book> lista = java.util.Arrays.asList(book);
		
		Page<Book> page = new PageImpl<Book>(lista, pageRequest, 1);

		when(repository.findAll(Mockito.any(Example.class), Mockito.any(PageRequest.class)))
		.thenReturn(page);
		
		Page<Book> result= service.find(book, pageRequest);
		
		assertThat(result.getTotalElements()).isEqualTo(1);
		assertThat(result.getContent()).isEqualTo(lista);
		assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
		assertThat(result.getPageable().getPageSize()).isEqualTo(10);
	}
	
	@Test
	@DisplayName("deve obter um livro pelo isbn")
	public void getBookIsbnTest() {
		String isbn = "1230";
		when(repository.findByIsbn(isbn)).thenReturn(Optional.of(Book.builder().id((long) 11).isbn(isbn).build()));
		
		Optional<Book> book = service.getBookByIsbn(isbn);
		
		assertThat(book.isPresent()).isTrue();
		assertThat(book.get().getId()).isEqualTo(11);
		assertThat(book.get().getIsbn()).isEqualTo(isbn);
		
		verify(repository, times(1)).findByIsbn(isbn);
	}
}
