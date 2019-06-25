package br.com.eskinfotechweb.algamoneyapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.eskinfotechweb.algamoneyapi.model.Pessoa;

public interface PessoaRepository extends JpaRepository<Pessoa, Long> {

}
