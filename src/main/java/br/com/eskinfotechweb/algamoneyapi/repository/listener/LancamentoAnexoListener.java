package br.com.eskinfotechweb.algamoneyapi.repository.listener;

import javax.persistence.PostLoad;

import org.springframework.util.StringUtils;

import br.com.eskinfotechweb.algamoneyapi.AlgamoneyApiApplication;
import br.com.eskinfotechweb.algamoneyapi.model.Lancamento;
import br.com.eskinfotechweb.algamoneyapi.storage.S3;

public class LancamentoAnexoListener {

	@PostLoad
	public void postLoad(Lancamento lancamento) {
		if (StringUtils.hasText(lancamento.getAnexo())) {
			S3 s3 = AlgamoneyApiApplication.getBean(S3.class);
			lancamento.setUrlAnexo(s3.configurarUrl(lancamento.getAnexo()));
		}
	}
	
}
