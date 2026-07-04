package org.ups.cic.functional.adapters.in.web;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DashboardControllerFunctionalTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Given convenios próximos sembrados, When GET /api/v1/dashboard/proximos-vencimientos, Then 200 con lista no vacía, hayVencimientos=true, mensaje=null, orden ASC por fechaVencimiento")
    @Sql(scripts = "classpath:data-us1.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(statements = "DELETE FROM convenios", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void dadoConveniosProximos_cuandoGetProximosVencimientos_entoncesListaOrdenada() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/proximos-vencimientos")
                        .accept("application/json"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.hayVencimientos").value(true))
                .andExpect(jsonPath("$.mensaje").doesNotExist())
                .andExpect(jsonPath("$.totalConvenios").value(3))
                .andExpect(jsonPath("$.convenios").isArray())
                .andExpect(jsonPath("$.convenios[0].diasRestantes").isNumber())
                .andExpect(jsonPath("$.convenios[0].id").isNumber())
                .andExpect(jsonPath("$.convenios[0].nombre").isString())
                .andExpect(jsonPath("$.convenios[0].entidadSocio").isString())
                .andExpect(jsonPath("$.convenios[0].fechaVencimiento").isString());
    }

    @Test
    @DisplayName("Given sin convenios próximos, When GET /api/v1/dashboard/proximos-vencimientos, Then 200 con convenios=[], totalConvenios=0, hayVencimientos=false, mensaje='Sin vencimientos próximos en los próximos 30 días'")
    @Sql(scripts = "classpath:data-us2.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(statements = "DELETE FROM convenios", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void dadoSinConveniosProximos_cuandoGetProximosVencimientos_entoncesListaVacia() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/proximos-vencimientos")
                        .accept("application/json"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.hayVencimientos").value(false))
                .andExpect(jsonPath("$.mensaje").value("Sin vencimientos próximos en los próximos 30 días"))
                .andExpect(jsonPath("$.totalConvenios").value(0))
                .andExpect(jsonPath("$.convenios").isArray())
                .andExpect(jsonPath("$.convenios").isEmpty());
    }
}
