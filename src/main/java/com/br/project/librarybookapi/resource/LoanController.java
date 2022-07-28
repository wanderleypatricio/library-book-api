package com.br.project.librarybookapi.resource;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.br.project.librarybookapi.dto.BookDTO;
import com.br.project.librarybookapi.dto.LoanDTO;
import com.br.project.librarybookapi.dto.LoanFilterDTO;
import com.br.project.librarybookapi.dto.ReturnedLoanDTO;
import com.br.project.librarybookapi.model.Book;
import com.br.project.librarybookapi.model.Loan;
import com.br.project.librarybookapi.service.BookService;
import com.br.project.librarybookapi.service.LoanService;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {

	private final BookService bookService;
	private final LoanService loanService;
	
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Long create(@RequestBody LoanDTO dto) {
		Book book = bookService.getBookByIsbn(dto.getIsbn()).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Book not found for passed isbn "));
		Loan entity = Loan.builder().book(book).customer(dto.getCustomer()).loanDate(LocalDate.now()).build();
		entity = loanService.save(entity);
		return entity.getId();
	}
	
	@PatchMapping("{id}")
	public void returnBook(@PathVariable Long id, @RequestBody ReturnedLoanDTO dto) {
		Loan loan = loanService.getById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		loan.setReturned(dto.getReturned());
		loanService.update(loan);
	}
	
	@GetMapping
	public Page<LoanDTO> find(LoanFilterDTO dto, Pageable pageRequest){
		Page<Loan> result = loanService.find(dto, pageRequest);
		List<LoanDTO> loans = result.getContent().stream().map(entity -> {
			Book book = entity.getBook();
			BookDTO bookDTO = BookDTO.builder().id(book.getId()).author(book.getAuthor()).title(book.getTitle()).isbn(book.getIsbn()).build();
			LoanDTO loanDTO = LoanDTO.builder().id(entity.getId()).book(bookDTO).customer(entity.getCustomer()).loanDate(entity.getLoanDate()).build();
			//loanDTO.setBook(bookDTO);
			return loanDTO;
		}).collect(Collectors.toList());
		
		return new PageImpl<LoanDTO>(loans, pageRequest, result.getTotalElements());
	}
}
