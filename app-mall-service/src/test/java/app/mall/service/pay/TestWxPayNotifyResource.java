package app.mall.service.pay;

import app.mall.wx.WxPayNotifyResource;
import io.nop.api.core.annotations.autotest.NopTestConfig;
import io.nop.api.core.annotations.core.OptionalBoolean;
import io.nop.autotest.junit.JunitBaseTestCase;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@NopTestConfig(localDb = true, initDatabaseSchema = OptionalBoolean.TRUE)
public class TestWxPayNotifyResource extends JunitBaseTestCase {

    @Inject
    WxPayNotifyResource wxPayNotifyResource;

    @Test
    void testNotifyDemoMode() {
        HttpHeaders headers = new HttpHeaders() {
            @Override
            public String getHeaderString(String name) { return null; }

            @Override
            public MultivaluedMap<String, String> getRequestHeaders() { return null; }

            @Override
            public List<String> getRequestHeader(String name) { return null; }

            @Override
            public MediaType getMediaType() { return null; }

            @Override
            public Locale getLanguage() { return null; }

            @Override
            public Map<String, Cookie> getCookies() { return null; }

            @Override
            public Date getDate() { return null; }

            @Override
            public int getLength() { return 0; }

            @Override
            public List<MediaType> getAcceptableMediaTypes() { return null; }

            @Override
            public List<Locale> getAcceptableLanguages() { return null; }
        };
        Response resp = wxPayNotifyResource.handleNotify(
                "{\"test\": true}", headers);
        assertEquals(200, resp.getStatus(),
                "demo mode should return 200");
    }
}
