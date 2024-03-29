package br.com.eskinfotechweb.algamoneyapi.repository.lancamento;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import br.com.eskinfotechweb.algamoneyapi.dto.LancamentoEstatisticaCategoria;
import br.com.eskinfotechweb.algamoneyapi.dto.LancamentoEstatisticaDia;
import br.com.eskinfotechweb.algamoneyapi.dto.LancamentoEstatisticaPessoa;
import br.com.eskinfotechweb.algamoneyapi.model.Lancamento;
import br.com.eskinfotechweb.algamoneyapi.repository.filter.LancamentoFilter;
import br.com.eskinfotechweb.algamoneyapi.repository.lancamento.projection.ResumoLancamento;

public interface LancamentoRepositoryQuery {

	public List<LancamentoEstatisticaPessoa> porPessoa(LocalDate dt_inicio, LocalDate dt_fim);
	public List<LancamentoEstatisticaCategoria> porCategoria(LocalDate mesReferencia);
	public List<LancamentoEstatisticaDia> porDia(LocalDate mesReferencia);
	
	public Page<Lancamento> filtrar(LancamentoFilter lancamentoFilter, Pageable pageable);
	public Page<ResumoLancamento> resumir(LancamentoFilter lancamentoFilter, Pageable pageable);
	
}
