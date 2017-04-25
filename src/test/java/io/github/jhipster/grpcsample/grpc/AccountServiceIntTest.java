package io.github.jhipster.grpcsample.grpc;

import io.github.jhipster.grpcsample.JhipsterGrpcSampleApplicationApp;
import io.github.jhipster.grpcsample.domain.Authority;
import io.github.jhipster.grpcsample.domain.PersistentToken;
import io.github.jhipster.grpcsample.domain.User;
import io.github.jhipster.grpcsample.repository.AuthorityRepository;
import io.github.jhipster.grpcsample.repository.PersistentTokenRepository;
import io.github.jhipster.grpcsample.repository.UserRepository;
import io.github.jhipster.grpcsample.security.AuthoritiesConstants;
import io.github.jhipster.grpcsample.service.MailService;
import io.github.jhipster.grpcsample.service.UserService;
import io.github.jhipster.grpcsample.web.rest.UserResourceIntTest;

import com.google.protobuf.Empty;
import com.google.protobuf.StringValue;
import io.grpc.Server;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = JhipsterGrpcSampleApplicationApp.class)
public class AccountServiceIntTest {

    private static final String DEFAULT_PASSWORD = "passjohndoe";
    private static final String UPDATED_PASSWORD = "passjhipster";

    private static final String DEFAULT_FIRSTNAME = "john";
    private static final String UPDATED_FIRSTNAME = "jhipsterFirstName";

    private static final String DEFAULT_LASTNAME = "doe";
    private static final String UPDATED_LASTNAME = "jhipsterLastName";

    private static final String DEFAULT_IMAGEURL = "http://placehold.it/50x50";
    private static final String UPDATED_IMAGEURL = "http://placehold.it/40x40";

    private static final String DEFAULT_LANGKEY = "en";
    private static final String UPDATED_LANGKEY = "fr";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private PersistentTokenRepository persistentTokenRepository;

    @Autowired
    private UserProtoMapper userProtoMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserService mockUserService;

    @Mock
    private MailService mockMailService;

    private Server mockServer;

    private Server mockUserServer;

    private AccountServiceGrpc.AccountServiceBlockingStub stub;

    private AccountServiceGrpc.AccountServiceBlockingStub userStub;

    @Before
    public void setUp() throws IOException {
        MockitoAnnotations.initMocks(this);
        doNothing().when(mockMailService).sendActivationEmail(anyObject());

        AccountService service =
            new AccountService(userRepository, userService, mockMailService, persistentTokenRepository, userProtoMapper);

        AccountService userService =
            new AccountService(userRepository, mockUserService, mockMailService, persistentTokenRepository, userProtoMapper);

        String uniqueServerName = "Mock server for " + AccountService.class;
        mockServer = InProcessServerBuilder
            .forName(uniqueServerName).directExecutor().addService(service).build().start();
        InProcessChannelBuilder channelBuilder =
            InProcessChannelBuilder.forName(uniqueServerName).directExecutor();
        stub = AccountServiceGrpc.newBlockingStub(channelBuilder.build());

        String uniqueUserServerName = "Mock user server for " + AccountService.class;
        mockUserServer = InProcessServerBuilder
            .forName(uniqueUserServerName).directExecutor().addService(userService).build().start();
        InProcessChannelBuilder userChannelBuilder =
            InProcessChannelBuilder.forName(uniqueUserServerName).directExecutor();
        userStub = AccountServiceGrpc.newBlockingStub(userChannelBuilder.build());

        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
    }

    @After
    public void tearDown() {
        mockServer.shutdownNow();
        mockUserServer.shutdown();
    }

    @Test
    public void testNonAuthenticatedUser() throws Exception {
        StringValue login = userStub.isAuthenticated(Empty.newBuilder().build());
        assertThat(login.getValue()).isEmpty();
    }

    @Test
    public void testAuthenticatedUser() throws Exception {
        Authentication authentication =
            new UsernamePasswordAuthenticationToken("grpc-authenticated-user", "password");
        SecurityContextHolder.getContext().setAuthentication(authentication);
        StringValue login = userStub.isAuthenticated(Empty.newBuilder().build());
        assertThat(login.getValue()).isEqualTo("grpc-authenticated-user");
    }

    @Test
    public void testGetExistingAccount() throws Exception {
        Set<Authority> authorities = new HashSet<>();
        Authority authority = new Authority();
        authority.setName(AuthoritiesConstants.ADMIN);
        authorities.add(authority);

        User user = new User();
        user.setLogin("grpc-existing-account");
        user.setFirstName(DEFAULT_FIRSTNAME);
        user.setLastName(DEFAULT_LASTNAME);
        user.setEmail("grpc-existing-account@example.com");
        user.setImageUrl(DEFAULT_IMAGEURL);
        user.setLangKey(DEFAULT_LANGKEY);
        user.setAuthorities(authorities);
        when(mockUserService.getUserWithAuthorities()).thenReturn(user);

        UserProto userProto = userStub.getAccount(Empty.newBuilder().build());
        assertThat(userProto.getLogin()).isEqualTo("grpc-existing-account");
        assertThat(userProto.getFirstName()).isEqualTo(DEFAULT_FIRSTNAME);
        assertThat(userProto.getLastName()).isEqualTo(DEFAULT_LASTNAME);
        assertThat(userProto.getEmail()).isEqualTo("grpc-existing-account@example.com");
        assertThat(userProto.getImageUrl()).isEqualTo(DEFAULT_IMAGEURL);
        assertThat(userProto.getLangKey()).isEqualTo(DEFAULT_LANGKEY);
        assertThat(userProto.getAuthoritiesList()).containsExactly(AuthoritiesConstants.ADMIN);
    }

    @Test
    public void testGetUnknownAccount() throws Exception {
        when(mockUserService.getUserWithAuthorities()).thenReturn(null);
        try {
            userStub.getAccount(Empty.newBuilder().build());
            failBecauseExceptionWasNotThrown(StatusRuntimeException.class);
        } catch (StatusRuntimeException e) {
            assertThat(e.getStatus().getCode()).isEqualTo(Status.Code.INTERNAL);
        }
    }

    @Test
    @Transactional
    public void testRegisterValid() throws Exception {
        UserProto validUser = UserProto.newBuilder()
            .setLogin("grpc-register-valid")
            .setPassword(DEFAULT_PASSWORD)
            .setFirstName(DEFAULT_FIRSTNAME)
            .setLastName(DEFAULT_LASTNAME)
            .setEmail("grpc-register-valid@example.com")
            .setActivated(true)
            .setImageUrl(DEFAULT_IMAGEURL)
            .setLangKey(DEFAULT_LANGKEY)
            .addAuthorities(AuthoritiesConstants.USER)
            .build();

        stub.registerAccount(validUser);

        assertThat(userRepository.findOneByLogin("grpc-register-valid")).isPresent();
    }

    @Test
    public void testRegisterInvalid() throws Exception {
        UserProto invalidUser = UserProto.newBuilder()
            .setLogin("grpc-register-inval!d")
            .setPassword(DEFAULT_PASSWORD)
            .setFirstName(DEFAULT_FIRSTNAME)
            .setLastName(DEFAULT_LASTNAME)
            .setEmail("grpc-register-invalid@example.com")
            .setActivated(true)
            .setImageUrl(DEFAULT_IMAGEURL)
            .setLangKey(DEFAULT_LANGKEY)
            .addAuthorities(AuthoritiesConstants.USER)
            .build();

        try {
            stub.registerAccount(invalidUser);
            failBecauseExceptionWasNotThrown(StatusRuntimeException.class);
        } catch (StatusRuntimeException e) {
            assertThat(e.getStatus().getCode()).isEqualTo(Status.Code.INVALID_ARGUMENT);
        }
        assertThat(userRepository.findOneByEmail("grpc-register-invalid@example.com")).isNotPresent();
    }

    @Test
    @Transactional
    public void testRegisterDuplicateLogin() throws Exception {
        // Good
        UserProto validUser = UserProto.newBuilder()
            .setLogin("grpc-register-duplicate-login")
            .setPassword(DEFAULT_PASSWORD)
            .setFirstName(DEFAULT_FIRSTNAME)
            .setLastName(DEFAULT_LASTNAME)
            .setEmail("grpc-register-duplicate-login@example.com")
            .setActivated(true)
            .setImageUrl(DEFAULT_IMAGEURL)
            .setLangKey(DEFAULT_LANGKEY)
            .addAuthorities(AuthoritiesConstants.USER)
            .build();

        // Duplicate login, different email
        UserProto duplicatedUser = UserProto.newBuilder()
            .setId(validUser.getId())
            .setLogin(validUser.getLogin())
            .setPassword(validUser.getPassword())
            .setFirstName(validUser.getFirstName())
            .setLastName(validUser.getLastName())
            .setEmail("grpc-register-duplicate-login2@example.com")
            .setActivated(validUser.getActivated())
            .setImageUrl(validUser.getImageUrl())
            .setLangKey(validUser.getLangKey())
            .setCreatedBy(validUser.getCreatedBy())
            .setCreatedDate(validUser.getCreatedDate())
            .setLastModifiedBy(validUser.getLastModifiedBy())
            .setLastModifiedDate(validUser.getLastModifiedDate())
            .addAuthorities(AuthoritiesConstants.USER)
            .build();

        stub.registerAccount(validUser);

        try {
            stub.registerAccount(duplicatedUser);
            failBecauseExceptionWasNotThrown(StatusRuntimeException.class);
        } catch (StatusRuntimeException e) {
            assertThat(e.getStatus().getCode()).isEqualTo(Status.Code.ALREADY_EXISTS);
            em.clear();
        }
        assertThat(userRepository.findOneByEmail("grpc-register-duplicate-login2@example.com")).isNotPresent();
    }

    @Test
    @Transactional
    public void testRegisterDuplicateEmail() throws Exception {
        // Good
        UserProto validUser = UserProto.newBuilder()
            .setLogin("grpc-register-duplicate-email")
            .setPassword(DEFAULT_PASSWORD)
            .setFirstName(DEFAULT_FIRSTNAME)
            .setLastName(DEFAULT_LASTNAME)
            .setEmail("grpc-register-duplicate-email@example.com")
            .setActivated(true)
            .setImageUrl(DEFAULT_IMAGEURL)
            .setLangKey(DEFAULT_LANGKEY)
            .addAuthorities(AuthoritiesConstants.USER)
            .build();

        // Duplicate login, different email
        UserProto duplicatedUser = UserProto.newBuilder()
            .setId(validUser.getId())
            .setLogin("grpc-register-duplicate-email2")
            .setPassword(validUser.getPassword())
            .setFirstName(validUser.getFirstName())
            .setLastName(validUser.getLastName())
            .setEmail(validUser.getEmail())
            .setActivated(validUser.getActivated())
            .setImageUrl(validUser.getImageUrl())
            .setLangKey(validUser.getLangKey())
            .setCreatedBy(validUser.getCreatedBy())
            .setCreatedDate(validUser.getCreatedDate())
            .setLastModifiedBy(validUser.getLastModifiedBy())
            .setLastModifiedDate(validUser.getLastModifiedDate())
            .addAuthorities(AuthoritiesConstants.USER)
            .build();

        stub.registerAccount(validUser);

        try {
            stub.registerAccount(duplicatedUser);
            failBecauseExceptionWasNotThrown(StatusRuntimeException.class);
        } catch (StatusRuntimeException e) {
            assertThat(e.getStatus().getCode()).isEqualTo(Status.Code.ALREADY_EXISTS);
            em.clear();
        }
        assertThat(userRepository.findOneByLogin("grpc-register-duplicate-email2")).isNotPresent();
    }

    @Test
    @Transactional
    public void testRegisterAdminIsIgnored() throws Exception {
        UserProto user = UserProto.newBuilder()
            .setLogin("grpc-register-admin-is-ignored")
            .setPassword(DEFAULT_PASSWORD)
            .setFirstName(DEFAULT_FIRSTNAME)
            .setLastName(DEFAULT_LASTNAME)
            .setEmail("grpc-register-admin-is-ignored@example.com")
            .setActivated(true)
            .setImageUrl(DEFAULT_IMAGEURL)
            .setLangKey(DEFAULT_LANGKEY)
            .addAuthorities(AuthoritiesConstants.ADMIN)
            .build();

        stub.registerAccount(user);

        Optional<User> userDup = userRepository.findOneByLogin("grpc-register-admin-is-ignored");
        assertThat(userDup).isPresent();
        assertThat(userDup.orElse(null).getAuthorities())
            .hasSize(1)
            .containsExactly(authorityRepository.findOne(AuthoritiesConstants.USER));
    }

    @Test
    @Transactional
    public void testActivateAccount() throws Exception {
        final String activationKey = "some activationKey";
        User user = UserResourceIntTest.createEntity(null);
        user.setLogin("grpc-activate-account");
        user.setEmail("grpc-activate-account@example.com");
        user.setActivated(false);
        user.setActivationKey(activationKey);

        userRepository.saveAndFlush(user);

        UserProto userProto = stub.activateAccount(StringValue.newBuilder().setValue(activationKey).build());
        assertThat(userProto.getLogin()).isEqualTo("grpc-activate-account");
        assertThat(userProto.getActivated()).isTrue();
    }

    @Test
    @Transactional
    public void testActivateAccountWithWrongKey() throws Exception {
        try {
            stub.activateAccount(StringValue.newBuilder().setValue("some wrong key").build());
            failBecauseExceptionWasNotThrown(StatusRuntimeException.class);
        } catch (StatusRuntimeException e) {
            assertThat(e.getStatus().getCode()).isEqualTo(Status.Code.INTERNAL);
        }
    }

    @Test
    @Transactional
    public void testSaveAccount() throws Exception {
        User user = UserResourceIntTest.createEntity(null);
        user.setLogin("grpc-save-account");
        user.setEmail("grpc-save-account@example.com");
        user.setAuthorities(new HashSet<>());
        userRepository.saveAndFlush(user);

        Authentication authentication =
            new UsernamePasswordAuthenticationToken(user.getLogin(), "password");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserProto userProto = UserProto.newBuilder()
            .setLogin(user.getLogin())
            .setPassword(UPDATED_PASSWORD)
            .setFirstName(UPDATED_FIRSTNAME)
            .setLastName(UPDATED_LASTNAME)
            .setEmail("grpc-save-account-updated@example.com")
            .setActivated(false)
            .setImageUrl(UPDATED_IMAGEURL)
            .setLangKey(UPDATED_LANGKEY)
            .addAuthorities(AuthoritiesConstants.ADMIN)
            .build();

        stub.saveAccount(userProto);

        User updatedUser = userRepository.findOneByLogin(userProto.getLogin()).orElse(null);
        assertThat(updatedUser.getFirstName()).isEqualTo(UPDATED_FIRSTNAME);
        assertThat(updatedUser.getLastName()).isEqualTo(UPDATED_LASTNAME);
        assertThat(updatedUser.getEmail()).isEqualTo(userProto.getEmail());
        assertThat(updatedUser.getLangKey()).isEqualTo(UPDATED_LANGKEY);
        assertThat(updatedUser.getPassword()).isEqualTo(user.getPassword());
        assertThat(updatedUser.getImageUrl()).isEqualTo(UPDATED_IMAGEURL);
        assertThat(updatedUser.getActivated()).isEqualTo(true);
        assertThat(updatedUser.getAuthorities()).isEmpty();
    }

    @Test
    public void testSaveInvalidEmail() throws Exception {
        User user = UserResourceIntTest.createEntity(null);
        user.setLogin("grpc-save-invalid-email");
        user.setEmail("grpc-save-invalid-email@example.com");
        user.setAuthorities(new HashSet<>());
        userRepository.saveAndFlush(user);

        Authentication authentication =
            new UsernamePasswordAuthenticationToken(user.getLogin(), "password");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserProto invalidUser = UserProto.newBuilder()
            .setPassword(UPDATED_PASSWORD)
            .setFirstName(UPDATED_FIRSTNAME)
            .setLastName(UPDATED_LASTNAME)
            .setEmail("Invalid email")
            .setActivated(false)
            .setImageUrl(UPDATED_IMAGEURL)
            .setLangKey(UPDATED_LANGKEY)
            .addAuthorities(AuthoritiesConstants.ADMIN)
            .build();

        try {
            stub.saveAccount(invalidUser);
            failBecauseExceptionWasNotThrown(StatusRuntimeException.class);
        } catch (StatusRuntimeException e) {
            assertThat(e.getStatus().getCode()).isEqualTo(Status.Code.INVALID_ARGUMENT);
            assertThat(userRepository.findOneByEmail("Invalid email")).isNotPresent();
        } finally {
            userRepository.delete(user);
        }
    }

    @Test
    @Transactional
    public void testSaveAccountExistingEmail() throws Exception {
        User user = UserResourceIntTest.createEntity(null);
        user.setLogin("grpc-save-account-existing-email");
        user.setEmail("grpc-save-account-existing-email@example.com");
        userRepository.saveAndFlush(user);

        Authentication authentication =
            new UsernamePasswordAuthenticationToken(user.getLogin(), "password");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        User anotherUser = UserResourceIntTest.createEntity(null);
        anotherUser.setLogin("grpc-save-account-existing-email2");
        anotherUser.setEmail("grpc-save-account-existing-email2@localhost.com");
        userRepository.saveAndFlush(anotherUser);

        UserProto userProto = UserProto.newBuilder()
            .setPassword(UPDATED_PASSWORD)
            .setFirstName(UPDATED_FIRSTNAME)
            .setLastName(UPDATED_LASTNAME)
            .setEmail("grpc-save-account-existing-email2@localhost.com")
            .setActivated(false)
            .setImageUrl(UPDATED_IMAGEURL)
            .setLangKey(UPDATED_LANGKEY)
            .addAuthorities(AuthoritiesConstants.ADMIN)
            .build();

        try {
            stub.saveAccount(userProto);
            failBecauseExceptionWasNotThrown(StatusRuntimeException.class);
        } catch (StatusRuntimeException e) {
            assertThat(e.getStatus().getCode()).isEqualTo(Status.Code.ALREADY_EXISTS);
        }
        User updatedUser = userRepository.findOneByLogin("grpc-save-account-existing-email").orElse(null);
        assertThat(updatedUser.getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    @Transactional
    public void testChangePassword() {
        User user = UserResourceIntTest.createEntity(null);
        user.setLogin("grpc-change-password");
        user.setEmail("grpc-change-password@example.com");
        userRepository.saveAndFlush(user);

        Authentication authentication =
            new UsernamePasswordAuthenticationToken(user.getLogin(), "password");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        stub.changePassword(StringValue.newBuilder().setValue("new password").build());

        User updatedUser = userRepository.findOneByLogin("grpc-change-password").orElse(null);
        assertThat(passwordEncoder.matches("new password", updatedUser.getPassword())).isTrue();
    }

    @Test
    @Transactional
    public void testChangePasswordTooSmall() {
        User user = UserResourceIntTest.createEntity(null);
        user.setLogin("grpc-change-password-too-small");
        user.setEmail("grpc-change-password-too-small@example.com");
        userRepository.saveAndFlush(user);

        Authentication authentication =
            new UsernamePasswordAuthenticationToken(user.getLogin(), "password");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        try {
            stub.changePassword(StringValue.newBuilder().setValue("foo").build());
            failBecauseExceptionWasNotThrown(StatusRuntimeException.class);
        } catch (StatusRuntimeException e) {
            assertThat(e.getStatus().getCode()).isEqualTo(Status.Code.INVALID_ARGUMENT);
        }
    }

    @Test
    @Transactional
    public void testChangePasswordTooLong() {
        User user = UserResourceIntTest.createEntity(null);
        user.setLogin("grpc-change-password-too-long");
        user.setEmail("grpc-change-password-too-long@example.com");
        userRepository.saveAndFlush(user);

        Authentication authentication =
            new UsernamePasswordAuthenticationToken(user.getLogin(), "password");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        try {
            String longPassword = Stream.generate(() -> String.valueOf("A")).limit(101).collect(Collectors.joining());
            assertThat(longPassword.length()).isEqualTo(101);
            stub.changePassword(StringValue.newBuilder().setValue(longPassword).build());
            failBecauseExceptionWasNotThrown(StatusRuntimeException.class);
        } catch (StatusRuntimeException e) {
            assertThat(e.getStatus().getCode()).isEqualTo(Status.Code.INVALID_ARGUMENT);
        }
    }

    @Test
    @Transactional
    public void testGetCurrentSessions() {
        User user = UserResourceIntTest.createEntity(null);
        user.setLogin("grpc-current-sessions");
        user.setEmail("grpc-current-sessions@example.com");
        userRepository.saveAndFlush(user);

        Authentication authentication =
            new UsernamePasswordAuthenticationToken(user.getLogin(), "password");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        PersistentToken token = new PersistentToken();
        token.setSeries("grpc-1111-1111");
        token.setUser(user);
        token.setTokenValue("1111-1111-data");
        token.setTokenDate(LocalDate.of(2017, 3, 23));
        token.setIpAddress("127.0.0.1");
        token.setUserAgent("Test agent");
        persistentTokenRepository.saveAndFlush(token);

        io.github.jhipster.grpcsample.grpc.PersistentToken tokenProto = stub.getCurrentSessions(Empty.newBuilder().build()).next();
        assertThat(tokenProto.getSeries()).isEqualTo("grpc-1111-1111");
        assertThat(tokenProto.getIpAddress()).isEqualTo("127.0.0.1");
        assertThat(tokenProto.getUserAgent()).isEqualTo("Test agent");
        assertThat(tokenProto.getTokenDate().getYear()).isEqualTo(2017);
        assertThat(tokenProto.getTokenDate().getMonth()).isEqualTo(3);
        assertThat(tokenProto.getTokenDate().getDay()).isEqualTo(23);
    }

    @Test
    @Transactional
    public void testInvalidateSession() {
        User user = UserResourceIntTest.createEntity(null);
        user.setLogin("grpc-invalidate-session");
        user.setEmail("grpc-invalidate-session@example.com");
        userRepository.saveAndFlush(user);

        Authentication authentication =
            new UsernamePasswordAuthenticationToken(user.getLogin(), "password");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        PersistentToken token = new PersistentToken();
        token.setSeries("grpc-2222-2222");
        token.setUser(user);
        token.setTokenValue("2222-2222-data");
        token.setTokenDate(LocalDate.now());
        token.setIpAddress("127.0.0.1");
        token.setUserAgent("Test agent");
        persistentTokenRepository.saveAndFlush(token);
        assertThat(persistentTokenRepository.findByUser(user)).hasSize(1);
        stub.invalidateSession(StringValue.newBuilder().setValue("grpc-2222-2222").build());
        assertThat(persistentTokenRepository.findByUser(user)).isEmpty();
    }

    @Test
    @Transactional
    public void testRequestPasswordReset() {
        User user = UserResourceIntTest.createEntity(null);
        user.setLogin("grpc-password-reset");
        user.setEmail("grpc-password-reset@example.com");
        userRepository.saveAndFlush(user);
        stub.requestPasswordReset(StringValue.newBuilder().setValue("grpc-password-reset@example.com").build());
    }

    @Test
    public void testRequestPasswordResetWrongEmail() {
        try {
            stub.requestPasswordReset(StringValue.newBuilder().setValue("grpc-password-reset-wrong-email@example.com").build());
            failBecauseExceptionWasNotThrown(StatusRuntimeException.class);
        } catch (StatusRuntimeException e) {
            assertThat(e.getStatus().getCode()).isEqualTo(Status.Code.INVALID_ARGUMENT);
        }
    }

    @Test
    @Transactional
    public void testFinishPasswordReset() {
        User user = UserResourceIntTest.createEntity(null);
        user.setLogin("grpc-finish-password-reset");
        user.setEmail("grpc-finish-password-reset@example.com");
        user.setResetDate(ZonedDateTime.now().plusDays(1));
        user.setResetKey("reset key");
        userRepository.saveAndFlush(user);

        stub.finishPasswordReset(KeyAndPassword.newBuilder()
            .setNewPassword("new password")
            .setKey("reset key").build()
        );

        User updatedUser = userRepository.findOneByLogin("grpc-finish-password-reset").orElse(null);
        assertThat(passwordEncoder.matches("new password", updatedUser.getPassword())).isTrue();
    }

    @Test
    public void testFinishPasswordResetPasswordTooSmall() {
        try {
            stub.finishPasswordReset(KeyAndPassword.newBuilder()
                .setNewPassword("foo")
                .setKey("reset key").build()
            );
            failBecauseExceptionWasNotThrown(StatusRuntimeException.class);
        } catch (StatusRuntimeException e) {
            assertThat(e.getStatus().getCode()).isEqualTo(Status.Code.INVALID_ARGUMENT);
        }
    }

    @Test
    @Transactional
    public void testFinishPasswordResetWrongKey() {
        User user = UserResourceIntTest.createEntity(null);
        user.setLogin("grpc-finish-password-reset-wrong-key");
        user.setEmail("grpc-finish-password-reset-wrong-key@example.com");
        userRepository.saveAndFlush(user);
        try {
            stub.finishPasswordReset(KeyAndPassword.newBuilder()
                .setNewPassword("new password")
                .setKey("wrong reset key").build()
            );
            failBecauseExceptionWasNotThrown(StatusRuntimeException.class);
        } catch (StatusRuntimeException e) {
            assertThat(e.getStatus().getCode()).isEqualTo(Status.Code.INTERNAL);
        }
    }

}
