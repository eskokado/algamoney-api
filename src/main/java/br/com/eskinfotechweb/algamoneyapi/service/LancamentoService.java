package br.com.eskinfotechweb.algamoneyapi.service;

import java.io.InputStream;
import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import br.com.eskinfotechweb.algamoneyapi.dto.LancamentoEstatisticaPessoa;
import br.com.eskinfotechweb.algamoneyapi.mail.Mailer;
import br.com.eskinfotechweb.algamoneyapi.model.Lancamento;
import br.com.eskinfotechweb.algamoneyapi.model.Pessoa;
import br.com.eskinfotechweb.algamoneyapi.model.Usuario;
import br.com.eskinfotechweb.algamoneyapi.repository.LancamentoRepository;
import br.com.eskinfotechweb.algamoneyapi.repository.PessoaRepository;
import br.com.eskinfotechweb.algamoneyapi.repository.UsuarioRepository;
import br.com.eskinfotechweb.algamoneyapi.service.exception.PessoaInexistenteOuInativaException;
import br.com.eskinfotechweb.algamoneyapi.storage.S3;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

@Service
public class LancamentoService {		
	
	private static final String DESTINATARIOS = "ROLE_PESQUISAR_LANCAMENTO";
	
	private static final Logger logger = LoggerFactory.getLogger(LancamentoService.class);
	
	@Autowired
	private LancamentoRepository lancamentoRepository;
	
	@Autowired
	private PessoaRepository pessoaRepository;
	
	@Autowired
	private UsuarioRepository usuarioRepository;
	
	@Autowired
	private Mailer mailer;
	
	@Autowired
	private S3 s3;
	
	//@Scheduled(fixedDelay = 1000 * 60 * 30)
	@Scheduled(cron = "0 0 6 * * *")
	public void avisarSobreLancamentosVencidos() {
		if (logger.isDebugEnabled()) {
			logger.debug("Preparando envio de e-mails de aviso de lançamentos vencidos.");
		}
		List<Lancamento> vencidos = lancamentoRepository
				.findByDataVencimentoLessThanEqualAndDataPagamentoIsNull(LocalDate.now());
		if (vencidos.isEmpty()) {
			logger.info("Sem lançamentos vencidos para aviso.");
			return;
		}
		
		logger.info("Existem {} lançamentos vencidos.", vencidos.size());
		
		List<Usuario> destinatarios = usuarioRepository
				.findByPermissoesDescricao(DESTINATARIOS);
		
		if (destinatarios.isEmpty()) {
			logger.warn("Existem lançamentos vencidos, mas o sistema não encontrou destinatários.");
			return;
		}
		
		mailer.avisarSobreLancamentosVencidos(vencidos, destinatarios);
		logger.info("Envio de e-mail de aviso concluído.");
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
		
		if (StringUtils.hasText(lancamento.getAnexo())) {
			s3.salvar(lancamento.getAnexo());
		}
		
		return lancamentoRepository.save(lancamento);		
	}
	
	public Lancamento atualizar(Long codigo, Lancamento lancamento) {
		Lancamento lancamentoSalvo = buscarLancamentoExistente(codigo);
		if (!lancamento.getPessoa().equals(lancamentoSalvo.getPessoa())){
			validarPessoa(lancamento);
		}
		
		if (StringUtils.isEmpty(lancamento.getAnexo()) 
				&& StringUtils.hasText(lancamentoSalvo.getAnexo())) {
			s3.remover(lancamentoSalvo.getAnexo());
		} else if (StringUtils.hasLength(lancamento.getAnexo())
				&& !lancamento.getAnexo().equals(lancamentoSalvo.getAnexo())) {
			s3.substituir(lancamentoSalvo.getAnexo(), lancamento.getAnexo());
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
