package lesson01.exercise;

import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;

import com.google.common.collect.ImmutableMap;

import io.jaegertracing.Configuration;
import io.jaegertracing.Configuration.ReporterConfiguration;
import io.jaegertracing.Configuration.SamplerConfiguration;
import io.jaegertracing.internal.JaegerTracer;



public class Hello {
    public static JaegerTracer initTracer(String service) {
        SamplerConfiguration samplerConfig = SamplerConfiguration.fromEnv().withType("const").withParam(1);
        ReporterConfiguration reporterConfig = ReporterConfiguration.fromEnv().withLogSpans(true);
        Configuration config = new Configuration(service).withSampler(samplerConfig).withReporter(reporterConfig);
        return config.getTracer();
    }

    private final Tracer tracer;

    private Hello(Tracer tracer) {
        this.tracer = tracer;
    }

    private void sayHello(String helloTo) {
    	// new span
        Span span = tracer.buildSpan("say-hello").start();
        // tag
        span.setTag("tag-hello-to", helloTo);
        String helloStr = String.format("Hello, %s!", helloTo);
        // log
        span.log(ImmutableMap.of("event", "string-format", "value", helloStr));
        System.out.println(helloStr);
        // log
        span.log(ImmutableMap.of("event", "println"));
        
        // finish span
        span.finish();
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            throw new IllegalArgumentException("Expecting one argument");
        }
        String helloTo = args[0];
        Tracer tracer = initTracer("hello-world");
        new Hello(tracer).sayHello(helloTo);
        //new Hello(GlobalTracer.get()).sayHello(helloTo);
    }
}




