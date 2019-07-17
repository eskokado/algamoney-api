package br.com.eskinfotechweb.algamoneyapi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.eskinfotechweb.algamoneyapi.model.Cidade;

@Repository
public interface CidadeRepository extends JpaRepository<Cidade, Long> {

	List<Cidade> findByEstadoCodigo(Long estadoCodigo);
	
}
