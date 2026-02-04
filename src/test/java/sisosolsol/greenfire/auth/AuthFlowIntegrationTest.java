package sisosolsol.greenfire.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import sisosolsol.greenfire.user.entity.UserAccount;
import sisosolsol.greenfire.user.repository.UserAccountRepository;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthFlowIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserAccountRepository userAccountRepository;

    private final String email = "it_" + UUID.randomUUID() + "@greenfire.local";
    private final String password = "Test!1234abcd";

    @AfterEach
    void cleanup() {
        userAccountRepository.findByEmail(email).ifPresent(userAccountRepository::delete);
    }

    private ResultActions signup(String email, String password) throws Exception {
        return mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", email,
                                "password", password
                        ))))
                .andDo(print())
                .andExpect(status().isOk())
                // signup이 void 응답이면 Content-Type이 없을 수 있음 → body만 비어있는지 확인
                .andExpect(content().string(""));
    }

    private String loginAndGetToken(String email, String password) throws Exception {
        String loginBody = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", email,
                                "password", password
                        ))))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(loginBody);
        String token = json.get("accessToken").asText();
        assertThat(token).isNotBlank();
        return token;
    }

    @Test
    void signup_then_db_findByEmail_then_login() throws Exception {
        // 1) signup
        signup(email, password);

        // 2) DB 저장 검증
        UserAccount saved = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new AssertionError("DB에 회원이 저장되지 않았습니다."));
        assertThat(saved.getEmail()).isEqualTo(email);
        assertThat(saved.getPasswordHash()).isNotBlank();

        // 3) login 검증
        String token = loginAndGetToken(email, password);
        assertThat(token).isNotBlank();
    }

    @Test
    void signup_then_login_then_access_me_should_200_if_me_is_fixed() throws Exception {
        signup(email, password);
        String token = loginAndGetToken(email, password);

        mockMvc.perform(get("/api/v1/user/profiles/me")
                        .header("Authorization", "Bearer " + token))
                .andDo(print())
                .andExpect(status().isOk());
    }
}
