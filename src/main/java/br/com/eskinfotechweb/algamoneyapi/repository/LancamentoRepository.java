package br.com.eskinfotechweb.algamoneyapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.eskinfotechweb.algamoneyapi.model.Lancamento;

public interface LancamentoRepository extends JpaRepository<Lancamento, Long> {

}
