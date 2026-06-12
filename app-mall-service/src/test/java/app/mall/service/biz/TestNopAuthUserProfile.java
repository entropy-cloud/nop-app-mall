package app.mall.service.biz;

import io.nop.api.core.annotations.autotest.NopTestConfig;
import io.nop.api.core.annotations.core.OptionalBoolean;
import io.nop.api.core.beans.ApiRequest;
import io.nop.api.core.beans.ApiResponse;
import io.nop.api.core.beans.FilterBeans;
import io.nop.api.core.beans.query.QueryBean;
import io.nop.auth.dao.entity.NopAuthUser;
import io.nop.autotest.junit.JunitBaseTestCase;
import io.nop.api.core.auth.IUserContext;
import io.nop.api.core.context.ContextProvider;
import io.nop.auth.core.login.UserContextImpl;
import io.nop.dao.api.IDaoProvider;
import io.nop.graphql.core.IGraphQLExecutionContext;
import io.nop.graphql.core.ast.GraphQLOperationType;
import io.nop.graphql.core.engine.IGraphQLEngine;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@NopTestConfig(localDb = true, initDatabaseSchema = OptionalBoolean.TRUE)
public class TestNopAuthUserProfile extends JunitBaseTestCase {

    @Inject
    IGraphQLEngine graphQLEngine;

    @Inject
    IDaoProvider daoProvider;

    @SuppressWarnings("unchecked")
    @Test
    public void testGetMyProfile() {
        NopAuthUser user = signUpUser("profileuser", "Profile@Pass1", "13800000001");
        setUserContext(user);

        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of());
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "NopAuthUser__getMyProfile", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "getMyProfile should succeed: " + result.getMsg());

        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertNotNull(data);
        assertEquals(user.getUserId(), data.get("userId"));
        assertNull(data.get("password"), "Password should not be returned");
        assertNull(data.get("salt"), "Salt should not be returned");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testUpdateMyProfile() {
        NopAuthUser user = signUpUser("updateuser", "Update@Pass1", "13800000002");
        setUserContext(user);

        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of(
                "nickName", "NewNick",
                "gender", 2
        ));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "NopAuthUser__updateMyProfile", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "updateMyProfile should succeed: " + result.getMsg());

        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertNotNull(data);
        assertEquals("NewNick", data.get("nickName"));
        assertEquals(2, data.get("gender"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testChangeSelfPassword() {
        NopAuthUser user = signUpUser("pwduser", "OldPass@123", "13800000003");
        setUserContext(user);

        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of(
                "oldPassword", "OldPass@123",
                "newPassword", "NewPass@456"
        ));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "NopAuthUser__changeSelfPassword", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "changeSelfPassword should succeed: " + result.getMsg());

        ApiRequest<Map<String, Object>> loginReq = ApiRequest.build(Map.of(
                "principalId", "pwduser",
                "principalSecret", "NewPass@456",
                "loginType", 1
        ));
        IGraphQLExecutionContext loginCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LoginApi__login", loginReq);
        ApiResponse<?> loginResult = graphQLEngine.executeRpc(loginCtx);
        assertEquals(0, loginResult.getStatus(), "Login with new password should succeed: " + loginResult.getMsg());
    }

    @Test
    public void testChangeSelfPasswordWrongOld() {
        NopAuthUser user = signUpUser("wrongpwduser", "RightPass@1", "13800000004");
        setUserContext(user);

        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of(
                "oldPassword", "WrongPass@1",
                "newPassword", "NewPass@456"
        ));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "NopAuthUser__changeSelfPassword", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertNotEquals(0, result.getStatus(), "changeSelfPassword with wrong old password should fail");
    }

    private void setUserContext(NopAuthUser user) {
        ContextProvider.getOrCreateContext().setUserId(user.getUserId());
        ContextProvider.getOrCreateContext().setUserName(user.getUserName());
        UserContextImpl userContext = new UserContextImpl();
        userContext.setUserId(user.getUserId());
        userContext.setUserName(user.getUserName());
        IUserContext.set(userContext);
    }

    private NopAuthUser signUpUser(String username, String password, String mobile) {
        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of(
                "username", username,
                "password", password,
                "mobile", mobile
        ));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LoginApi__signUp", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "signUp helper failed: " + result.getMsg());

        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq("userName", username));
        return (NopAuthUser) daoProvider.daoFor(NopAuthUser.class)
                .findAllByQuery(query).stream().findFirst().orElse(null);
    }
}
