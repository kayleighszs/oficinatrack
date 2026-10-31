package br.com.oficinatrack.integration;

import br.com.oficinatrack.IntegrationTestBase;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OpenApiIntegrationTest extends IntegrationTestBase {

    @Test
    @DisplayName("expõe a documentação OpenAPI")
    void apiDocsDisponivel() throws Exception {
        Logger logger = (Logger) LoggerFactory.getLogger("org.springdoc.core.utils.SpringDocUtils");
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);

        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").exists())
                .andExpect(jsonPath("$.paths['/ordens-servico'].get.parameters[?(@.name == 'page')]").exists())
                .andExpect(jsonPath("$.paths['/ordens-servico'].get.parameters[?(@.name == 'size')]").exists())
                .andExpect(jsonPath("$.paths['/ordens-servico'].get.parameters[?(@.name == 'sort')]").exists())
                .andExpect(jsonPath("$.paths['/ordens-servico'].get.parameters[?(@.name == 'pageable')]").isEmpty())
                .andExpect(jsonPath("$.paths['/ordens-servico/{id}/historico-status'].get.responses['200'].content['*/*'].schema.type").value("array"))
                .andExpect(jsonPath("$.paths['/publico/ordens-servico/{numeroOs}/status'].get.security[0].publicAccessToken").exists())
                .andExpect(jsonPath("$.paths['/publico/ordens-servico/{numeroOs}/status'].get.parameters[?(@.name == 'Authorization')]").isEmpty())
                .andExpect(jsonPath("$.paths['/auth/login'].post.security").isEmpty())
                .andExpect(jsonPath("$.paths['/veiculos'].post.responses['201']").exists())
                .andExpect(jsonPath("$.paths['/veiculos'].post.responses['400'].content['application/json'].schema['$ref']")
                        .value("#/components/schemas/ErroResponse"))
                .andExpect(jsonPath("$.paths['/pecas'].post.responses['201']").exists())
                .andExpect(jsonPath("$.paths['/ordens-servico'].post.responses['201']").exists())
                .andExpect(jsonPath("$.paths['/ordens-servico/{ordemId}/acessos-publicos'].post.responses['201']").exists())
                .andExpect(jsonPath("$.components.securitySchemes.publicAccessToken").exists())
                .andExpect(jsonPath("$.components.schemas.HttpHeaders").doesNotExist())
                .andExpect(jsonPath("$.components.schemas.ProblemDetail").doesNotExist());

        assertThat(appender.list)
                .extracting(ILoggingEvent::getFormattedMessage)
                .noneMatch(message -> message.contains("Json Processing Exception occurred"));

        logger.detachAppender(appender);
        appender.stop();
    }

    @Test
    @DisplayName("executa paginação de ordens conforme documentada no Swagger")
    void paginacaoDeOrdensDisponivel() throws Exception {
        mockMvc.perform(get("/ordens-servico")
                        .header("Authorization", "Bearer " + tokenGestor())
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "criadoEm,DESC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }
}
