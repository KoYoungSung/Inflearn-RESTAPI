package ko.springrestapi.configs;

import ko.springrestapi.accounts.Account;
import ko.springrestapi.accounts.AccountRole;
import ko.springrestapi.accounts.AccountService;
import ko.springrestapi.common.BaseControllerTest;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthServerConfigTest extends BaseControllerTest {

    @Autowired
    AccountService accountService;

    @SneakyThrows
    @Test
    @DisplayName("인증 토큰을 발급 받는 테스트")
    public void getAuthToken() {


        String username = "ko@naver.com";
        String password = "ko";
        Account ko = Account.builder()
                .email(username)
                .password(password)
                .roles(Set.of(AccountRole.ADMIN, AccountRole.USER))
                .build();
        this.accountService.saveAccount(ko);

        String clientId = "myApp";
        String clientSecret = "pass";


        this.mockMvc.perform(MockMvcRequestBuilders.post("/oauth/token")
                .with(httpBasic(clientId, clientSecret))
                .param("username", username)
                .param("password", password)
                .param("grant_type", "password")
        );

    }

}