package br.com.eskinfotechweb.algamoneyapi.service;

import java.io.InputStream;
import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import br.com.eskinfotechweb.algamoneyapi.dto.LancamentoEstatisticaPessoa;
import br.com.eskinfotechweb.algamoneyapi.mail.Mailer;
import br.com.eskinfotechweb.algamoneyapi.model.Lancamento;
import br.com.eskinfotechweb.algamoneyapi.model.Pessoa;
import br.com.eskinfotechweb.algamoneyapi.model.Usuario;
import br.com.eskinfotechweb.algamoneyapi.repository.LancamentoRepository;
import br.com.eskinfotechweb.algamoneyapi.repository.PessoaRepository;
import br.com.eskinfotechweb.algamoneyapi.repository.UsuarioRepository;
import br.com.eskinfotechweb.algamoneyapi.service.exception.PessoaInexistenteOuInativaException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

@Service
public class LancamentoService {
	
	private static final String DESTINATARIOS = "ROLE_PESQUISAR_LANCAMENTO";
	
	@Autowired
	private LancamentoRepository lancamentoRepository;
	
	@Autowired
	private PessoaRepository pessoaRepository;
	
	@Autowired
	private UsuarioRepository usuarioRepository;
	
	@Autowired
	private Mailer mailer;
	
	//@Scheduled(fixedDelay = 1000 * 60 * 30)
	@Scheduled(cron = "0 0 6 * * *")
	public void avisarSobreLancamentosVencidos() {
		List<Lancamento> vencidos = lancamentoRepository
				.findByDataVencimentoLessThanEqualAndDataPagamentoIsNull(LocalDate.now());
		
		List<Usuario> destinatarios = usuarioRepository
				.findByPermissoesDescricao(DESTINATARIOS);
		
		mailer.avisarSobreLancamentosVencidos(vencidos, destinatarios);
	} 

	public byte[] relatorioPorPessoa(LocalDate dt_inicio, LocalDate dt_fim) throws Exception {
		List<LancamentoEstatisticaPessoa> dados = lancamentoRepository.porPessoa(dt_inicio, dt_fim);
		
		Map<String, Object> parametros = new HashMap<>();
		parametros.put("DT_INICIO", Date.valueOf(dt_inicio));
		parametros.put("DT_FIM", Date.valueOf(dt_fim));
		parametros.put("REPORT_LOCALE", new Locale("pt", "BR"));
		
		InputStream inputStream = this.getClass().getResourceAsStream(
				"/relatorios/lancamentos-por-pessoa.jasper");
		
		JasperPrint jasperPrint = JasperFillManager.fillReport(
				inputStream, parametros, new JRBeanCollectionDataSource(dados));
		
		return JasperExportManager.exportReportToPdf(jasperPrint);
		
	}
	
	public Lancamento salvar(Lancamento lancamento) {
		validarPessoa(lancamento);
		
		return lancamentoRepository.save(lancamento);		
	}
	
	public Lancamento atualizar(Long codigo, Lancamento lancamento) {
		Lancamento lancamentoSalvo = buscarLancamentoExistente(codigo);
		if (!lancamento.getPessoa().equals(lancamentoSalvo.getPessoa())){
			validarPessoa(lancamento);
		}
		BeanUtils.copyProperties(lancamento, lancamentoSalvo, "codigo");
		return lancamentoRepository.save(lancamentoSalvo);
	}

	private void validarPessoa(Lancamento lancamento) {
		Pessoa pessoa = null;
		if (lancamento.getPessoa().getCodigo() != null) {			
			pessoa = pessoaRepository.findOne(lancamento.getPessoa().getCodigo());
		}
		if (pessoa == null || pessoa.isInativo()) {
			throw new PessoaInexistenteOuInativaException();
		}
	}

	private Lancamento buscarLancamentoExistente(Long codigo) {
		Lancamento lancamentoSalvo = lancamentoRepository.findOne(codigo);
		if (lancamentoSalvo == null) {
			throw new IllegalArgumentException();
		}
		return lancamentoSalvo;
	}
}
