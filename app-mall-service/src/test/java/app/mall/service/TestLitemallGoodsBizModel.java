package app.mall.service;

import io.nop.api.core.annotations.autotest.EnableSnapshot;
import io.nop.api.core.beans.ApiRequest;
import io.nop.api.core.context.ContextProvider;
import io.nop.api.core.util.FutureHelper;
import io.nop.autotest.junit.JunitAutoTestCase;
import io.nop.graphql.core.IGraphQLExecutionContext;
import io.nop.graphql.core.ast.GraphQLOperationType;
import io.nop.graphql.core.engine.IGraphQLEngine;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;

public class TestLitemallGoodsBizModel extends JunitAutoTestCase {

    @Inject
    IGraphQLEngine graphQLEngine;

    @EnableSnapshot
    @Test
    public void testSave() {
        ContextProvider.getOrCreateContext().setUserId("0");
        ContextProvider.getOrCreateContext().setUserName("test");

        ApiRequest<?> request = input("request.json5", ApiRequest.class);
        IGraphQLExecutionContext context = graphQLEngine.newRpcContext(GraphQLOperationType.mutation,
                "LitemallGoods__save", request);
        Object result = FutureHelper.syncGet(graphQLEngine.executeRpcAsync(context));
        output("response.json5", result);
    }
}
