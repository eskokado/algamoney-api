package br.com.eskinfotechweb.algamoneyapi.repository.lancamento;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import br.com.eskinfotechweb.algamoneyapi.dto.LancamentoEstatisticaCategoria;
import br.com.eskinfotechweb.algamoneyapi.dto.LancamentoEstatisticaDia;
import br.com.eskinfotechweb.algamoneyapi.dto.LancamentoEstatisticaPessoa;
import br.com.eskinfotechweb.algamoneyapi.model.Categoria_;
import br.com.eskinfotechweb.algamoneyapi.model.Lancamento;
import br.com.eskinfotechweb.algamoneyapi.model.Lancamento_;
import br.com.eskinfotechweb.algamoneyapi.model.Pessoa_;
import br.com.eskinfotechweb.algamoneyapi.repository.filter.LancamentoFilter;
import br.com.eskinfotechweb.algamoneyapi.repository.lancamento.projection.ResumoLancamento;

public class LancamentoRepositoryImpl implements LancamentoRepositoryQuery {

	@PersistenceContext
	private EntityManager manager;
	
	@Override
	public List<LancamentoEstatisticaPessoa> porPessoa(LocalDate dt_inicio, LocalDate dt_fim) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<LancamentoEstatisticaPessoa> criteria =  builder.createQuery(LancamentoEstatisticaPessoa.class);

		Root<Lancamento> root = criteria.from(Lancamento.class);
		
		criteria.select(builder
				.construct(LancamentoEstatisticaPessoa.class, 
					root.get(Lancamento_.tipo),
					root.get(Lancamento_.pessoa),
					builder.sum(root.get(Lancamento_.valor))
				)
		);
		
		criteria.where(
					builder.greaterThanOrEqualTo(root.get(Lancamento_.dataVencimento), dt_inicio),
					builder.lessThanOrEqualTo(root.get(Lancamento_.dataVencimento), dt_fim)
				);
		criteria.groupBy(root.get(Lancamento_.tipo), root.get(Lancamento_.pessoa));
		
		TypedQuery<LancamentoEstatisticaPessoa> query = manager.createQuery(criteria);
		
		return query.getResultList();
	}
	
	@Override
	public List<LancamentoEstatisticaDia> porDia(LocalDate mesReferencia) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<LancamentoEstatisticaDia> criteria =  builder.createQuery(LancamentoEstatisticaDia.class);

		Root<Lancamento> root = criteria.from(Lancamento.class);
		
		criteria.select(builder
				.construct(LancamentoEstatisticaDia.class, 
					root.get(Lancamento_.tipo),
					root.get(Lancamento_.dataVencimento),
					builder.sum(root.get(Lancamento_.valor))
				)
		);
		LocalDate primeiroDia = mesReferencia.minusYears(1).withDayOfMonth(1);
		LocalDate ultimoDia = mesReferencia.withDayOfMonth(mesReferencia.lengthOfMonth());
		
		criteria.where(
					builder.greaterThanOrEqualTo(root.get(Lancamento_.dataVencimento), primeiroDia),
					builder.lessThanOrEqualTo(root.get(Lancamento_.dataVencimento), ultimoDia)
				);
		criteria.groupBy(root.get(Lancamento_.tipo), root.get(Lancamento_.dataVencimento));
		
		TypedQuery<LancamentoEstatisticaDia> query = manager.createQuery(criteria);
		
		return query.getResultList();
	}
	
	@Override
	public List<LancamentoEstatisticaCategoria> porCategoria(LocalDate mesReferencia) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<LancamentoEstatisticaCategoria> criteria =  builder.createQuery(LancamentoEstatisticaCategoria.class);

		Root<Lancamento> root = criteria.from(Lancamento.class);
		
		criteria.select(builder
				.construct(LancamentoEstatisticaCategoria.class, 
					root.get(Lancamento_.categoria),
					builder.sum(root.get(Lancamento_.valor))
				)
		);
		LocalDate primeiroDia = mesReferencia.minusYears(1).withDayOfMonth(1);
		LocalDate ultimoDia = mesReferencia.withDayOfMonth(mesReferencia.lengthOfMonth());
		
		criteria.where(
					builder.greaterThanOrEqualTo(root.get(Lancamento_.dataVencimento), primeiroDia),
					builder.lessThanOrEqualTo(root.get(Lancamento_.dataVencimento), ultimoDia)
				);
		criteria.groupBy(root.get(Lancamento_.categoria));
		
		TypedQuery<LancamentoEstatisticaCategoria> query = manager.createQuery(criteria);
		
		return query.getResultList();
	}
	
	@Override
	public Page<Lancamento> filtrar(LancamentoFilter lancamentoFilter, Pageable pageable) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Lancamento> criteria =  builder.createQuery(Lancamento.class);
		Root<Lancamento> root = criteria.from(Lancamento.class);
		
		// Criar as restrições
		Predicate[] predicates = criarRestricoes(lancamentoFilter, builder, root);
		
		criteria.where(predicates);
				
		TypedQuery<Lancamento> query = manager.createQuery(criteria);
		adicionarRestricoesDePaginacao(query, pageable);
		
		return new PageImpl<>(query.getResultList(), pageable, total(lancamentoFilter))  ;
	}

	@Override
	public Page<ResumoLancamento> resumir(LancamentoFilter lancamentoFilter, Pageable pageable) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<ResumoLancamento> criteria =  builder.createQuery(ResumoLancamento.class);
		Root<Lancamento> root = criteria.from(Lancamento.class);

		criteria.select(builder.construct(ResumoLancamento.class
				, root.get(Lancamento_.codigo), root.get(Lancamento_.descricao)
				, root.get(Lancamento_.dataVencimento), root.get(Lancamento_.dataPagamento)
				, root.get(Lancamento_.valor), root.get(Lancamento_.tipo)
				, root.get(Lancamento_.categoria).get(Categoria_.nome)
				, root.get(Lancamento_.pessoa).get(Pessoa_.nome)));
		
		// Criar as restrições
		Predicate[] predicates = criarRestricoes(lancamentoFilter, builder, root);
		
		criteria.where(predicates);
				
		TypedQuery<ResumoLancamento> query = manager.createQuery(criteria);
		adicionarRestricoesDePaginacao(query, pageable);
		
		return new PageImpl<>(query.getResultList(), pageable, total(lancamentoFilter))  ;
	}
	
	private Long total(LancamentoFilter lancamentoFilter) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Long> criteria = builder.createQuery(Long.class);
		
		Root<Lancamento> root = criteria.from(Lancamento.class);
		
		Predicate[] predicates = criarRestricoes(lancamentoFilter, builder, root);
		criteria.where(predicates);
		
		criteria.select(builder.count(root));
		return manager.createQuery(criteria).getSingleResult();
	}

	private void adicionarRestricoesDePaginacao(TypedQuery<?> query, Pageable pageable) {
		int paginaAtual = pageable.getPageNumber();
		int totalRegistrosPorPagina = pageable.getPageSize();
		int primeiroRegistroDaPagina = paginaAtual * totalRegistrosPorPagina;
		
		query.setFirstResult(primeiroRegistroDaPagina);
		query.setMaxResults(totalRegistrosPorPagina);				
	}

	private Predicate[] criarRestricoes(LancamentoFilter lancamentoFilter, CriteriaBuilder builder,
			Root<Lancamento> root) {
		List<Predicate> predicates = new ArrayList<>();
		
		if (!StringUtils.isEmpty(lancamentoFilter.getDescricao())) {
			predicates.add(builder.like(
					builder.lower(root.get(Lancamento_.descricao)), "%" + lancamentoFilter.getDescricao().toLowerCase() + "%"
					));
		}
		
		if (lancamentoFilter.getDataVencimentoDe() != null) {
			predicates.add(builder.greaterThanOrEqualTo(root.get(Lancamento_.dataVencimento), lancamentoFilter.getDataVencimentoDe()
					));
		}
		
		if (lancamentoFilter.getDataVencimentoAte() != null) {
			predicates.add(builder.lessThanOrEqualTo(root.get(Lancamento_.dataVencimento), lancamentoFilter.getDataVencimentoAte()
					));
		}
		
		return predicates.toArray(new Predicate[predicates.size()]);
	}


}
