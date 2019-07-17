package br.com.eskinfotechweb.algamoneyapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.eskinfotechweb.algamoneyapi.model.Estado;

@Repository
public interface EstadoRepository extends JpaRepository<Estado, Long> {

}
