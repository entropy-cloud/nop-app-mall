package app.mall.service.biz;

import io.nop.api.core.annotations.autotest.NopTestConfig;
import io.nop.api.core.annotations.core.OptionalBoolean;
import io.nop.api.core.beans.ApiRequest;
import io.nop.api.core.beans.ApiResponse;
import io.nop.api.core.beans.FilterBeans;
import io.nop.api.core.beans.query.QueryBean;
import io.nop.auth.dao.entity.NopAuthUser;
import io.nop.autotest.junit.JunitBaseTestCase;
import io.nop.dao.api.IDaoProvider;
import io.nop.graphql.core.IGraphQLExecutionContext;
import io.nop.graphql.core.ast.GraphQLOperationType;
import io.nop.graphql.core.engine.IGraphQLEngine;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@NopTestConfig(localDb = true, initDatabaseSchema = OptionalBoolean.TRUE)
public class TestLoginApiSignUp extends JunitBaseTestCase {

    @Inject
    IGraphQLEngine graphQLEngine;

    @Inject
    IDaoProvider daoProvider;

    @SuppressWarnings("unchecked")
    @Test
    public void testSignUpSuccess() {
        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of(
                "username", "testuser",
                "password", "Test@123456",
                "mobile", "13800138000"
        ));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LoginApi__signUp", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "signUp should succeed: " + result.getMsg());

        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertNotNull(data);
        assertNotNull(data.get("userId"), "Should return userId");

        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq("userName", "testuser"));
        NopAuthUser user = (NopAuthUser) daoProvider.daoFor(NopAuthUser.class)
                .findAllByQuery(query).stream().findFirst().orElse(null);
        assertNotNull(user, "User should be created in DB");
        assertEquals("testuser", user.getNickName());
        assertEquals(1, user.getUserType());
        assertNotNull(user.getSalt(), "Password should be salted");
        assertNotEquals("Test@123456", user.getPassword(), "Password should be hashed");
    }

    @Test
    public void testSignUpDuplicateUsername() {
        signUpUser("dupuser", "Pass@word1", "13900139000");

        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of(
                "username", "dupuser",
                "password", "Pass@word2",
                "mobile", "13900139001"
        ));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LoginApi__signUp", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertNotEquals(0, result.getStatus());
        assertTrue(result.getMsg().contains("username-exists") || result.getMsg().contains("用户名已注册"),
                "Error should indicate username exists: " + result.getMsg());
    }

    @Test
    public void testSignUpEmptyUsername() {
        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of(
                "username", "",
                "password", "Pass@word1",
                "mobile", "13800138001"
        ));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LoginApi__signUp", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertNotEquals(0, result.getStatus());
    }

    @Test
    public void testSignUpEmptyPassword() {
        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of(
                "username", "nopwduser",
                "password", "",
                "mobile", "13800138002"
        ));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LoginApi__signUp", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertNotEquals(0, result.getStatus());
    }

    @Test
    public void testSignUpEmptyMobile() {
        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of(
                "username", "nomobileuser",
                "password", "Pass@word1",
                "mobile", ""
        ));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LoginApi__signUp", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertNotEquals(0, result.getStatus());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testLoginAfterSignUp() {
        signUpUser("loginuser", "Login@Pass1", "13700137000");

        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of(
                "principalId", "loginuser",
                "principalSecret", "Login@Pass1",
                "loginType", 1
        ));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LoginApi__login", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "Login after signUp should succeed: " + result.getMsg());

        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertNotNull(data.get("accessToken"));
    }

    @Test
    public void testDisabledUserCannotLogin() {
        signUpUser("disableduser", "Disabled@1", "13600136000");

        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq("userName", "disableduser"));
        NopAuthUser user = (NopAuthUser) daoProvider.daoFor(NopAuthUser.class)
                .findAllByQuery(query).stream().findFirst().orElse(null);
        assertNotNull(user);
        user.setStatus(0);
        daoProvider.daoFor(NopAuthUser.class).updateEntityDirectly(user);

        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of(
                "principalId", "disableduser",
                "principalSecret", "Disabled@1",
                "loginType", 1
        ));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LoginApi__login", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertNotEquals(0, result.getStatus(), "Disabled user should not be able to login");
    }

    private void signUpUser(String username, String password, String mobile) {
        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of(
                "username", username,
                "password", password,
                "mobile", mobile
        ));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LoginApi__signUp", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "signUp helper failed: " + result.getMsg());
    }
}
