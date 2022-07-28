package com.br.project.librarybookapi.service.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.br.project.librarybookapi.dto.LoanFilterDTO;
import com.br.project.librarybookapi.exception.BusinessException;
import com.br.project.librarybookapi.model.Book;
import com.br.project.librarybookapi.model.Loan;
import com.br.project.librarybookapi.repository.LoanRepository;
import com.br.project.librarybookapi.service.LoanService;

@Service
public class LoanServiceImpl implements LoanService {

	private LoanRepository repository;

	public LoanServiceImpl(LoanRepository repository) {
		this.repository = repository;
	}
	
	@Override
	public Loan save(Loan loan) {
		if(repository.existsByBookAndNotReturned(loan.getBook())) {
			throw new BusinessException("Book already loaned");
		}
		return repository.save(loan);
	}

	@Override
	public Optional<Loan> getById(Long id) {
		return repository.findById(id);
	}

	@Override
	public Loan update(Loan loan) {
		// TODO Auto-generated method stub
		return repository.save(loan);
	}

	@Override
	public Page<Loan> find(LoanFilterDTO filter, Pageable pageable) {
		return repository.findBookIsbnOrCustomer(filter.getIsbn(), filter.getCustomer(), pageable);
	}

	@Override
	public Page<Loan> getLoansByBook(Book book, Pageable pageable) {
		return repository.findByBook(book, pageable);
	}

	@Override
	public List<Loan> getAllLateLoans() {
		final Integer loanDays = 4;
		LocalDate threeDaysAgo = LocalDate.now().minusDays(loanDays);
		
		return repository.findByLoanDateLessThanNotReturned(threeDaysAgo);
	}
	

}
