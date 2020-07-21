package curso.springboot.springboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.Ordered;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
@EntityScan(basePackages = "curso.springboot.model")
@ComponentScan(basePackages = { "curso.*" })
@EnableJpaRepositories(basePackages = { "curso.springboot.repository" })
@EnableTransactionManagement
@EnableWebMvc //habilitando as funçoes MVC para redirecionar a tela de login
public class SpringbootApplication implements WebMvcConfigurer {

	public static void main(String[] args) {
		SpringApplication.run(SpringbootApplication.class, args);

		// Metodo para criptografar as senhas manualmente
		/*
		 * BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(); String result =
		 * encoder.encode("123"); System.out.println(result);
		 */

	}

	@Override // Redirecionar a tela de login do Spring para uma tela de login custumizada
	public void addViewControllers(ViewControllerRegistry registry) {
		registry.addViewController("/login").setViewName("/login"); // mapeando as paginas de login
		registry.setOrder(Ordered.LOWEST_PRECEDENCE);
	}

}
