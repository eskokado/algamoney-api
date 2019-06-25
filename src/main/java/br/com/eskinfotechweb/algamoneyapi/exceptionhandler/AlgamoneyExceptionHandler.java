package br.com.eskinfotechweb.algamoneyapi.exceptionhandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class AlgamoneyExceptionHandler extends ResponseEntityExceptionHandler {

	@Autowired
	private MessageSource messageSource;

	@Override
	protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {

		String mensagemUsuario = messageSource.getMessage("mensagem.invalida", null, LocaleContextHolder.getLocale());
		String mensageDesenvolvedor = ex.getCause().toString();
		return handleExceptionInternal(ex, new Erro(mensagemUsuario, mensageDesenvolvedor), headers, HttpStatus.BAD_REQUEST, request);
	}

	public static class Erro {
		private String mensagemUsuario;
		private String mensageDesenvolvedor;

		public Erro(String mensagemUsuario, String mensageDesenvolvedor) {
			super();
			this.mensagemUsuario = mensagemUsuario;
			this.mensageDesenvolvedor = mensageDesenvolvedor;
		}

		public String getMensagemUsuario() {
			return mensagemUsuario;
		}

		public void setMensagemUsuario(String mensagemUsuario) {
			this.mensagemUsuario = mensagemUsuario;
		}

		public String getMensageDesenvolvedor() {
			return mensageDesenvolvedor;
		}

		public void setMensageDesenvolvedor(String mensageDesenvolvedor) {
			this.mensageDesenvolvedor = mensageDesenvolvedor;
		}

	}
}
