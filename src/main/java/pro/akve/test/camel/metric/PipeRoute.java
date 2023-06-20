package pro.akve.test.camel.metric;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.http;

@Component
public class PipeRoute extends RouteBuilder {
    @Override
    public void configure() {
        from("direct:simple")
                .setHeader(Exchange.HTTP_METHOD, simple("GET"))
                .to(http("0.0.0.0:34001/stub")
                        .authenticationPreemptive(true)
                        .authMethod("Basic")
                        .authUsername("login")
                        .authPassword("my-super-secret-password"));
    }
}
