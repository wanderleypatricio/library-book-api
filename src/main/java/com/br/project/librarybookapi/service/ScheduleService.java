package com.br.project.librarybookapi.service;

import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.br.project.librarybookapi.model.Loan;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ScheduleService {
	private static final String CRON_LATE_LOANS = "0 0 0 1/1 * ?";
	
	private final LoanService loanService;
	private final EmailService emailService;
	
	@Scheduled(cron = CRON_LATE_LOANS)
	public void sendMailToLateLoan() {
		List<Loan> allLateLoans = loanService.getAllLateLoans();
		List<String> mailsList = allLateLoans.stream()
				.map(loan -> loan.getCustomerEmail())
				.collect(Collectors.toList());
		
		String message = "Atenção! Emprestimo de um livro ainda pendente. Por favor entregue o livro!";
		emailService.sendEmails(message, mailsList);
	}
}
