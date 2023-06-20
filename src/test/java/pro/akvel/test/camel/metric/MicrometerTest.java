package pro.akvel.test.camel.metric;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.prometheus.client.exporter.common.TextFormat;
import org.apache.camel.ProducerTemplate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.metrics.AutoConfigureMetrics;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import pro.akve.test.camel.metric.Application;

import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;

@SpringBootTest(webEnvironment = DEFINED_PORT, classes = Application.class)
@AutoConfigureMockMvc
@AutoConfigureMetrics
class MicrometerTest {

    public final static WireMockServer toServiceMockServer;
    public static final String MY_SUPER_SECRET_PASSWORD = "my-super-secret-password";

    static {
        toServiceMockServer = new WireMockServer(wireMockConfig()
                .port(34001)
        );
        toServiceMockServer.start();
        toServiceMockServer.stubFor(
                get(WireMock.urlPathMatching("/stub"))
                        .willReturn(ok())
        );
    }

    @Autowired
    PrometheusMeterRegistry prometheusMeterRegistry;

    @Autowired
    private ProducerTemplate producerTemplate;

    @Test
    void getPrometheus() throws Exception {
        producerTemplate.sendBody("direct:simple", "test");

        List<String> badMetrics = new ArrayList<>();

        Assertions.assertAll();

        var iterator = prometheusMeterRegistry.getPrometheusRegistry().metricFamilySamples();
        Writer writer = new StringWriter(16);
        //same writer used Spring Boot Actuator
        TextFormat.write004(writer, iterator);
        var result = writer.toString();

        assertThat(result, not(containsString(MY_SUPER_SECRET_PASSWORD)));
    }
}
