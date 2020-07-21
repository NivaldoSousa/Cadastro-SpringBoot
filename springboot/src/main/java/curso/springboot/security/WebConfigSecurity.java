package curso.springboot.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration // anotaçao que fornece configuraçao do spring
@EnableWebSecurity // ativa varios sistemas de seguraça
public class WebConfigSecurity extends WebSecurityConfigurerAdapter {

    @Autowired
	private ImplementacaoUserDetailsService implementacaoUserDetailsService;
	
	@Override // configura as solitaçoes de acesso por Http
	protected void configure(HttpSecurity http) throws Exception {
	http.csrf()
	.disable() //Desativa as configuraçoes padrao de memoria
	.authorizeRequests() //Permitir restringir acessos
	.antMatchers(HttpMethod.GET, "/").permitAll() //Qualquer usuario tem acesso a pagina inicial
	.antMatchers(HttpMethod.GET, "/cadastropessoa").hasAnyRole("ADMIN") // SO quem for admin tem acesso a essa pagina
	.anyRequest().authenticated().and().formLogin().permitAll() // permite qualquer usuario
	.loginPage("/login") // redireciona pagina de login customizada
	.defaultSuccessUrl("/cadastropessoa") // sucesso no login vai para o cadastro
	.failureUrl("/login?error=true") // falha no login 
	.and().logout().logoutSuccessUrl("/login") // Mapeia URL de logout e invalida usuario autenticado, volta para a pagina de login
	.logoutRequestMatcher(new AntPathRequestMatcher("/logout"));
	}
	
	@Override // Cria autenticaçao do usuario com banco de dados ou em memoria
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
	auth.userDetailsService(implementacaoUserDetailsService)
	.passwordEncoder(new BCryptPasswordEncoder());
	
	//so para didatica para saber como criptografar uma senha
	/*auth.inMemoryAuthentication().passwordEncoder(new BCryptPasswordEncoder())
	.withUser("nivaldo")
	.password("$2a$10$TkxhCXM1pDrXxAzFd/EkOOuOwjG8n9WDpdYdB4hE7xMUYwwX0BZxO")
	.roles("ADMIN");*/
	}

	@Override // Ignora URL especificas
	public void configure(WebSecurity web) throws Exception {
		web.ignoring().antMatchers("/materialize/**");

	}

}
