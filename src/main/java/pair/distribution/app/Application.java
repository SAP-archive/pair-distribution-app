package pair.distribution.app;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

@SpringBootApplication
@EnableGlobalMethodSecurity(securedEnabled = true)
public class Application extends SpringBootServletInitializer 
{

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) 
    {
        return application.sources(Application.class);
    }
    
	public static void main(String[] args)
	{
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken("user", "N/A", AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_USER"));
		SecurityContextHolder.getContext().setAuthentication(authentication);
        SpringApplication.run(Application.class, args);
	}
}
