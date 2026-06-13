package app.mall.service.biz;

import io.nop.api.core.annotations.autotest.NopTestConfig;
import io.nop.api.core.annotations.core.OptionalBoolean;
import io.nop.api.core.beans.ApiRequest;
import io.nop.api.core.beans.ApiResponse;
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
public class TestPasswordReset extends JunitBaseTestCase {
    @Inject
    IGraphQLEngine graphQLEngine;
    @Inject
    IDaoProvider daoProvider;

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

    @Test
    public void testSendResetCode() {
        signUpUser("resetuser1", "Pass@1234", "13800138000");

        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of(
                "mobile", "13800138000"
        ));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LoginApi__sendResetCode", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "sendResetCode failed: " + result.getMsg());
    }

    @Test
    public void testSendResetCodeMobileNotFound() {
        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of(
                "mobile", "99999999999"
        ));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LoginApi__sendResetCode", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertNotEquals(0, result.getStatus(), "Expected error for non-existent mobile");
    }

    @Test
    public void testResetPassword() {
        signUpUser("resetuser2", "Pass@1234", "13800138001");

        ApiRequest<Map<String, Object>> sendReq = ApiRequest.build(Map.of(
                "mobile", "13800138001"
        ));
        IGraphQLExecutionContext sendCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LoginApi__sendResetCode", sendReq);
        ApiResponse<?> sendResult = graphQLEngine.executeRpc(sendCtx);
        assertEquals(0, sendResult.getStatus(), "sendResetCode helper failed: " + sendResult.getMsg());

        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of(
                "mobile", "13800138001",
                "code", "wrong_code",
                "newPassword", "NewPass@123"
        ));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LoginApi__resetPassword", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertNotEquals(0, result.getStatus(), "Expected error for invalid reset code");
    }

    @Test
    public void testResetPasswordEmptyMobile() {
        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of(
                "mobile", "",
                "code", "some_code",
                "newPassword", "NewPass@123"
        ));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LoginApi__resetPassword", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertNotEquals(0, result.getStatus(), "Expected error for empty mobile");
    }
}
