package br.com.eskinfotechweb.algamoneyapi.config.property;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("algamoney")
public class AlgamoneyApiProperty {
	
	@Value("${algamoney.origin-permitida}")
	private String originPermitida;

	private final Seguranca seguranca = new Seguranca();
	
	public String getOriginPermitida() {
		return originPermitida;
	}
	
	public Seguranca getSeguranca() {
		return seguranca;
	}
	
	public static class Seguranca {
		private boolean enableHttps;

		public boolean isEnableHttps() {
			return enableHttps;
		}

		public void setEnableHttps(boolean enableHttps) {
			this.enableHttps = enableHttps;
		}
		
		
	}
	
}
