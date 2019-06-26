package br.com.eskinfotechweb.algamoneyapi.repository.lancamento;

import java.util.List;

import br.com.eskinfotechweb.algamoneyapi.model.Lancamento;
import br.com.eskinfotechweb.algamoneyapi.repository.filter.LancamentoFilter;

public interface LancamentoRepositoryQuery {

	public List<Lancamento> filtrar(LancamentoFilter lancamentoFilter);
	
}
