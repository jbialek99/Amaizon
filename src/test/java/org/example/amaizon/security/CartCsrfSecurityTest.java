package org.example.amaizon.security;

import org.example.amaizon.services.CartService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CartCsrfSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CartService cartService;

    private static final String HOST = "amaizon.localhost.pl";

    @Test
    void addWithoutCsrfIsForbidden() throws Exception {
        mockMvc.perform(post("/cart/add")
                        .header("Host", HOST)
                        .param("productCountryStableId", UUID.randomUUID().toString())
                        .param("qty", "2"))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateWithoutCsrfIsForbidden() throws Exception {
        mockMvc.perform(post("/cart/update")
                        .header("Host", HOST)
                        .param("productCountryStableId", UUID.randomUUID().toString())
                        .param("qty", "3"))
                .andExpect(status().isForbidden());
    }

    @Test
    void removeWithoutCsrfIsForbidden() throws Exception {
        mockMvc.perform(post("/cart/remove")
                        .header("Host", HOST)
                        .param("productCountryStableId", UUID.randomUUID().toString()))
                .andExpect(status().isForbidden());
    }

    @Test
    void addWithCsrfIsAllowed() throws Exception {
        mockMvc.perform(post("/cart/add")
                        .header("Host", HOST)
                        .with(csrf())
                        .param("productCountryStableId", UUID.randomUUID().toString())
                        .param("qty", "2"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void updateWithCsrfIsAllowed() throws Exception {
        mockMvc.perform(post("/cart/update")
                        .header("Host", HOST)
                        .with(csrf())
                        .param("productCountryStableId", UUID.randomUUID().toString())
                        .param("qty", "3"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void removeWithCsrfIsAllowed() throws Exception {
        mockMvc.perform(post("/cart/remove")
                        .header("Host", HOST)
                        .with(csrf())
                        .param("productCountryStableId", UUID.randomUUID().toString()))
                .andExpect(status().is3xxRedirection());
    }
}
