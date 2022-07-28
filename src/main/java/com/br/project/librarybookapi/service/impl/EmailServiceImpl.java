package com.br.project.librarybookapi.service.impl;

import java.util.List;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.br.project.librarybookapi.service.EmailService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService{

	private final JavaMailSender javaMailSender;
	@Override
	public void sendEmails(String message, List<String> mailsList) {
		String[] mails = mailsList.toArray(new String[mailsList.size()]);
		SimpleMailMessage mailMessage = new SimpleMailMessage();
		mailMessage.setFrom("wpatricio26@gmail.com");
		mailMessage.setSubject("Emprestimo de livro não devolvido");
		mailMessage.setText(message);
		mailMessage.setTo(mails);
		javaMailSender.send(mailMessage);
	}

}
