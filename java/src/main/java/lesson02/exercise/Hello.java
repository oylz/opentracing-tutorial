package lesson02.exercise;

import io.opentracing.Span;
import io.opentracing.Tracer;

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

    private String formatString(Span root_span, String helloTo) {
    	Span span = tracer.buildSpan("formatString").asChildOf(root_span).start();
    	
        String helloStr = String.format("Hello, %s!", helloTo);
        span.log(ImmutableMap.of("event", "format string", "anykey", helloStr));
        span.finish();
        return helloStr;
    }

    private void printHello(Span root_span, String helloStr) {
    	Span span = tracer.buildSpan("printHello").asChildOf(root_span).start();
        // log
        span.log(ImmutableMap.of("event", "begin println, and sleep 10s", "anykey", helloStr));
        System.out.println(helloStr);
        nsleep(span);
        // log
        span.log(ImmutableMap.of("event", "after println"));
        span.finish();
    }
    private void nsleep(Span parent) {
    	Span span = tracer.buildSpan("nsleep").asChildOf(parent).start();
    	span.log(ImmutableMap.of("event", "begin sleep"));
        try {
        	Thread.sleep(10000);
        }
        catch(Exception e) {
        	e.printStackTrace();
        }
        span.log(ImmutableMap.of("event", "end sleep"));
        span.finish();
    }
    private void sayHello(String helloTo) {
    	// new span
        Span span = tracer.buildSpan("lesson01.exercise.Hello.sayHello").start();
        // tag
        span.setTag("add a tag", helloTo);
        
        
        String helloStr = formatString(span, helloTo);
        printHello(span, helloStr);
        
        // finish span
        span.finish();
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            throw new IllegalArgumentException("Expecting one argument");
        }
        String helloTo = args[0];
        Tracer tracer = initTracer("lesson2");
        new Hello(tracer).sayHello(helloTo);
        //new Hello(GlobalTracer.get()).sayHello(helloTo);
    }
}




