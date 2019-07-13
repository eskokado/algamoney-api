package br.com.eskinfotechweb.algamoneyapi.mail;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import br.com.eskinfotechweb.algamoneyapi.model.Lancamento;
import br.com.eskinfotechweb.algamoneyapi.model.Usuario;
import br.com.eskinfotechweb.algamoneyapi.repository.LancamentoRepository;

@Component
public class Mailer {

	@Autowired
	private JavaMailSender mailSender;
	
	@Autowired
	private TemplateEngine thymeleaf;
	
	@Autowired
	private LancamentoRepository repo;
	
//	@EventListener
//	private void teste(ApplicationReadyEvent event) {
//		this.enviarEmail("edsonskok@gmail.com", 
//				Arrays.asList("eskokado@gmail.com"), 
//				"Testando", "Olá<br/>Teste ok");
//		System.out.println("Terminado o envio de e-mail...");
//	}

	
//	 @EventListener private void teste(ApplicationReadyEvent event) { 
//		 String template = "mail/aviso-lancamentos-vencidos.html";
	  
//		 List<Lancamento> lista = repo.findAll();
		  
//		 Map<String, Object> variaveis = new HashMap<>(); variaveis.put("lancamentos",
//		 lista);
		 
//		 this.enviarEmail("edsonskok@gmail.com", Arrays.asList("eskokado@gmail.com"),
//		 "Testando Thymeleaf", template, variaveis);
		 
//		 System.out.println("Terminado o envio de e-mail..."); 
//	 }
	 
	 

	public void avisarSobreLancamentosVencidos(
		List<Lancamento> vencidos, List<Usuario> destinatarios
	) {
		
		String template = "mail/aviso-lancamentos-vencidos.html";
		
		Map<String, Object> variaveis = new HashMap<>();
		variaveis.put("lancamentos", vencidos);
		
//		List<String> emails = destinatarios.stream()
//				.map(u -> u.getEmail())
//				.collect(Collectors.toList());
	
		List<String> emails = Arrays.asList("eskokado@gmail.com");
		
		this.enviarEmail("edsonskok@gmail.com", emails, 
				"Lançamentos vencidos", template, variaveis);
	}
	
	public void enviarEmail(
			String remetente, List<String> destinatarios, String assunto, 
			String template, Map<String, Object> variaveis
		) {
		Context context = new Context(new Locale("pt", "BR"));
		
		variaveis.entrySet().forEach(
				e -> context.setVariable(e.getKey() , e.getValue()));
		
		String mensagem = thymeleaf.process(template, context);
		
		this.enviarEmail(remetente, destinatarios, assunto, mensagem);
	}
	
	public void enviarEmail(
				String remetente, List<String> destinatarios, String assunto, String mensagem
			) {
		try {
			MimeMessage mimeMessage = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");
			helper.setFrom(remetente);
			helper.setTo(destinatarios.toArray(new String[destinatarios.size()]));
			helper.setSubject(assunto);
			helper.setText(mensagem);
			
			mailSender.send(mimeMessage);
		} catch (MessagingException e) {
			throw new RuntimeException("Problemas com o envio de e-mail", e);
		}
		
	}
}
