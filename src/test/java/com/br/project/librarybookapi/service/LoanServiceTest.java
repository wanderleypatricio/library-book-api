package com.br.project.librarybookapi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.br.project.librarybookapi.dto.LoanFilterDTO;
import com.br.project.librarybookapi.exception.BusinessException;
import com.br.project.librarybookapi.model.Book;
import com.br.project.librarybookapi.model.Loan;
import com.br.project.librarybookapi.repository.LoanRepository;
import com.br.project.librarybookapi.service.impl.LoanServiceImpl;


@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class LoanServiceTest {

	@MockBean
	LoanRepository repository;
	
	LoanService service;

	@BeforeEach
	public void setUp() {
		this.service = new LoanServiceImpl(repository);
	}
	
	@Test
	@DisplayName("Deve salvar um emprestimo")
	public void salveLoanTest() {
		Book book = Book.builder().id((long) 11).build();
		String customer = "Fulano";
		
		Loan loanSaving = Loan.builder()
				.book(book)
				.customer(customer)
				.loanDate(LocalDate.now())
				.build();
		
		Loan savedLoan = Loan.builder()
				.id((long) 11)
				.book(book)
				.customer(customer)
				.loanDate(LocalDate.now())
				.build();
		when(repository.existsByBookAndNotReturned(book)).thenReturn(false);
		when(repository.save(loanSaving)).thenReturn(savedLoan);
		
		Loan loan = service.save(loanSaving);
		
		assertThat(loan.getId()).isEqualTo(savedLoan.getId());
		assertThat(loan.getBook().getId()).isEqualTo(savedLoan.getBook().getId());
		assertThat(loan.getCustomer()).isEqualTo(savedLoan.getCustomer());
		assertThat(loan.getLoanDate()).isEqualTo(savedLoan.getLoanDate());
	}
	
	@Test
	@DisplayName("Deve lançar erro de negócio ao salvar um emprestimo com livro já emprestado")
	public void LoanedBookSaveTest() {
		Book book = Book.builder().id((long) 11).build();
		String customer = "Fulano";
		
		Loan loanSaving = Loan.builder()
				.book(book)
				.customer(customer)
				.loanDate(LocalDate.now())
				.build();
		
		when(repository.existsByBookAndNotReturned(book)).thenReturn(true);
		
		Throwable exception = catchThrowable(() -> service.save(loanSaving));
		
		assertThat(exception).isInstanceOf(BusinessException.class).hasMessage("Book already loaned");
		
		verify(repository, never()).save(loanSaving);
	}
	
	@Test
	@DisplayName("deve obter informações do empréstimo pelo id")
	public void getLoanDetailsTest() throws Exception{
		Loan loan = createLoan();
		Mockito.when(repository.findById(loan.getId())).thenReturn(Optional.of(loan));
		Optional<Loan> result = service.getById(loan.getId());
		
		assertThat(result.isPresent()).isTrue();
		assertThat(result.get().getId()).isEqualTo(loan.getId());
		assertThat(result.get().getCustomer()).isEqualTo(loan.getCustomer());
		assertThat(result.get().getBook()).isEqualTo(loan.getBook());
		assertThat(result.get().getLoanDate()).isEqualTo(loan.getLoanDate());
	}
	
	@Test
	@DisplayName("deve atualizar um empréstimo")
	public void updateLoanTest() {
		Loan loan = createLoan();
		
		when(repository.save(loan)).thenReturn(loan);
		
		Loan updatedLoan = service.update(loan);
		
		assertThat(updatedLoan.getReturned()).isTrue();
		verify(repository).save(loan);
	}
	
	@Test
	@DisplayName("deve cadastrar um empréstimo de livro")
	public static Loan createLoan() {
		Book book = Book.builder().id((long) 11).build();
		String customer = "Fulano";
		Long id = (long) 11;
		return Loan.builder()
				.book(book)
				.id(id)
				.customer(customer)
				.loanDate(LocalDate.now())
				.build();
	}
	
	@Test
	@DisplayName("Deve filtrar emprestimos pelas propriedades")
	public void findByLoanTest() {
		LoanFilterDTO filterDTO = LoanFilterDTO.builder().customer("Fulano").isbn("123").build();
		
		Loan loan = createLoan();
		
		PageRequest pageRequest = PageRequest.of(0, 100);
		
		List<Loan> lista = Arrays.asList(loan);
		
		Page<Loan> page = new PageImpl<Loan>(lista, pageRequest, lista.size());
		
		when(repository.findBookIsbnOrCustomer(
				Mockito.anyString(), 
				Mockito.anyString(), 
				Mockito.any(PageRequest.class)))
		.thenReturn(page);
		
		Page<Loan> result = service.find(filterDTO, pageRequest) ;
		
		assertThat(result.getTotalElements()).isEqualTo(1);
		assertThat(result.getContent()).isEqualTo(lista);
		assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
		assertThat(result.getPageable().getPageSize()).isEqualTo(100);
	}
}
