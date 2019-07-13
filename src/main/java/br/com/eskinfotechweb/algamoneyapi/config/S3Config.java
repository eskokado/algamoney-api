package br.com.eskinfotechweb.algamoneyapi.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.BucketLifecycleConfiguration;
import com.amazonaws.services.s3.model.CreateBucketRequest;
import com.amazonaws.services.s3.model.Tag;
import com.amazonaws.services.s3.model.lifecycle.LifecycleFilter;
import com.amazonaws.services.s3.model.lifecycle.LifecycleTagPredicate;

import br.com.eskinfotechweb.algamoneyapi.config.property.AlgamoneyApiProperty;
import br.com.eskinfotechweb.algamoneyapi.storage.S3;

@Configuration
public class S3Config {

	private static final Logger logger = LoggerFactory.getLogger(S3Config.class);
	
	@Autowired
	private AlgamoneyApiProperty property;
	
	@Bean
	public AmazonS3 amazonS3() {
		logger.info("Configurando as credenciais de S3");
		AWSCredentials credenciais = new BasicAWSCredentials(
				property.getS3().getAccessKeyId(), property.getS3().getSecretAccessKey());
		
		AmazonS3 amazonS3 = AmazonS3ClientBuilder.standard()
				.withCredentials(new AWSStaticCredentialsProvider(credenciais))
				.withRegion(Regions.DEFAULT_REGION)
				.build();
		
		if (!amazonS3.doesBucketExist(property.getS3().getBucket())) {
			logger.info("Criando Bucket na S3");
			amazonS3.createBucket(new CreateBucketRequest(property.getS3().getBucket()));
			
			BucketLifecycleConfiguration.Rule regraExpiracao = 
					new BucketLifecycleConfiguration.Rule()
					.withId("Regra de expiração de arquivos temporários")
					.withFilter(new LifecycleFilter(
							new LifecycleTagPredicate(new Tag("expirar", "true"))))
					.withExpirationInDays(1)
					.withStatus(BucketLifecycleConfiguration.ENABLED);
			
			BucketLifecycleConfiguration configuration = 
					new BucketLifecycleConfiguration().withRules(regraExpiracao);
			
			amazonS3.setBucketLifecycleConfiguration(property.getS3().getBucket(), configuration);
		}			

		logger.info("Termino de configuração na S3");
		
		return amazonS3;
	}
	
}
