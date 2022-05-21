package com.br.project.librarybookapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.br.project.librarybookapi.model.Book;

public interface BookRepository extends JpaRepository<Book, Long>{

	boolean existsByIsbn(String isbn);
	
}
