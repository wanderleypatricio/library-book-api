package com.br.project.librarybookapi.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.br.project.librarybookapi.model.Book;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public class BookRespositoryTest {

	@Autowired
	TestEntityManager entityManager;
	
	@Autowired
	BookRepository repository;
	
	@Test
	@DisplayName("Deve retornar verdadeiro quando existir um livro na base com isbn informado")
	public void returnTrueWhenIsbnExists() {
		String isbn = "123";
		
		Book book = Book.builder().author("Wanderley").title("A volta dos que não foram").isbn(isbn).build();
		entityManager.persist(book);
		
		boolean exists = repository.existsByIsbn(isbn);
		
		assertThat(exists).isTrue();
	}
	
	@Test
	@DisplayName("Deve retornar falso quando não existir um livro na base com isbn informado")
	public void returnFalseWhenIsbnDoesntExist() {
		String isbn = "123";
		
		
		boolean exists = repository.existsByIsbn(isbn);
		
		assertThat(exists).isFalse();
	}
}
