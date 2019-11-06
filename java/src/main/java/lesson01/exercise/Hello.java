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
    	// will add a tag [sampler.type]
        SamplerConfiguration samplerConfig = SamplerConfiguration.fromEnv().withType("const").withParam(1);
        // will add a tag [sampler.param]
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
        Span span = tracer.buildSpan("lesson01.exercise.Hello.sayHello").start();
        // tag
        span.setTag("add a tag", helloTo);
        String helloStr = String.format("Hello, %s!", helloTo);
        // log
        span.log(ImmutableMap.of("event", "begin println", "anykey", helloStr));
        System.out.println(helloStr);
        // log
        span.log(ImmutableMap.of("event", "after println"));
        
        // finish span
        span.finish();
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            throw new IllegalArgumentException("Expecting one argument");
        }
        String helloTo = args[0];
        Tracer tracer = initTracer("lesson1");
        new Hello(tracer).sayHello(helloTo);
        //new Hello(GlobalTracer.get()).sayHello(helloTo);
    }
}




