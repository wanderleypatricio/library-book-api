package com.br.project.librarybookapi.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.br.project.librarybookapi.api.resource.BookControllerTest;
import com.br.project.librarybookapi.dto.BookDTO;
import com.br.project.librarybookapi.model.Book;
import com.br.project.librarybookapi.model.Loan;
import com.br.project.librarybookapi.resource.BookController;
import com.br.project.librarybookapi.service.BookService;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public class LoanRepositoryTest {

	
	@Autowired
	private LoanRepository repository;
	
	@Autowired
	private TestEntityManager entityManager;
	
	@Test
	@DisplayName("deve verificar se existe empréstimo não devolvido para o livro.")
	public void existsByBookAndNotReturnedTest() {
		Book book = BookRespositoryTest.createNewBook("123");
		entityManager.persist(book);
		
		Loan loan = Loan.builder().book(book).customer("Fulano").loanDate(LocalDate.now()).build();
		entityManager.persist(loan);
		
		boolean exist = repository.existsByBookAndNotReturned(book);
		
		assertThat(exist).isTrue();
	}
	
	@Test
	@DisplayName("Deve buscar empréstimo pelo isbn do livro ou customer")
	public void findByBookIsbnOrCustomerTest() {
		
		
		Page<Loan> result = repository.findBookIsbnOrCustomer("123", "Fulano", PageRequest.of(0, 100));
		
		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getTotalElements()).isEqualTo(1);
		assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
		assertThat(result.getPageable().getPageSize()).isEqualTo(100);
	}
	
	@Test
	@DisplayName("Deve obter empréstimos cujo a data emprestimo for menor ou igual a três dias atrás e não retornados")
	public void findByLoanDateLessThanNotReturnedTest(){
		Loan loan = createAndPersistLoan(LocalDate.now().minusDays(5));
		
		List<Loan> result = repository.findByLoanDateLessThanNotReturned(LocalDate.now().minusDays(4));
		
		assertThat(result).hasSize(1).contains(loan);
	}

	@Test
	@DisplayName("Deve retornar vazio quando não ouver empréstimos atrasados")
	public void notFindByLoanDateLessThanNotReturnedTest(){
		Loan loan = createAndPersistLoan(LocalDate.now());
		
		List<Loan> result = repository.findByLoanDateLessThanNotReturned(LocalDate.now().minusDays(4));
		
		assertThat(result).isEmpty();
	}
	
	
	public Loan createAndPersistLoan(LocalDate loanDate) {
		Book book = Book.builder().author("Artur").title("A volta dos que não foram").isbn("123").build();
		
		entityManager.persist(book);
		
		Loan loan = Loan.builder().book(book).customer("Fulano").loanDate(loanDate).build();
		
		entityManager.persist(loan);
		return loan;
	}
}