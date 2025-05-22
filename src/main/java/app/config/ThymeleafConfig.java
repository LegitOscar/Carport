package app.config;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

public class ThymeleafConfig {
    public static TemplateEngine templateEngine() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();

        resolver.setPrefix("/templates/");         // vigtigt: før / betyder classpath-mappe
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML");          // vigtigt: siger at det er HTML-filer
        resolver.setCharacterEncoding("UTF-8");    // så æ, ø, å virker korrekt
        resolver.setCacheable(false);              // slå cache fra ved udvikling

        TemplateEngine engine = new TemplateEngine();
        engine.setTemplateResolver(resolver);

        return engine;
    }
}
