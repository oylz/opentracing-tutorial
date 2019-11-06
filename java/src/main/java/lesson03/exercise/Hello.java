package lesson03.exercise;

import java.io.IOException;

import com.google.common.collect.ImmutableMap;

import io.jaegertracing.Configuration;
import io.jaegertracing.Configuration.ReporterConfiguration;
import io.jaegertracing.Configuration.SamplerConfiguration;
import io.jaegertracing.internal.JaegerTracer;
import io.opentracing.Span;
import io.opentracing.Tracer;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Hello {

    private final Tracer tracer;
    private final OkHttpClient client;

    private Hello(Tracer tracer) {
        this.tracer = tracer;
        this.client = new OkHttpClient();
    }

    private String getHttp(int port, String path, String param, String value) {
        try {
            HttpUrl url = new HttpUrl.Builder().scheme("http").host("localhost").port(port).addPathSegment(path)
                    .addQueryParameter(param, value).build();
            Request.Builder requestBuilder = new Request.Builder().url(url);
            Request request = requestBuilder.build();
            Response response = client.newCall(request).execute();
            if (response.code() != 200) {
                throw new RuntimeException("Bad HTTP result: " + response);
            }
            return response.body().string();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }     
            

    public static JaegerTracer initTracer(String service) {
    	// will add a tag [sampler.type]
        SamplerConfiguration samplerConfig = SamplerConfiguration.fromEnv().withType("const").withParam(1);
        // will add a tag [sampler.param]
        ReporterConfiguration reporterConfig = ReporterConfiguration.fromEnv().withLogSpans(true);
        Configuration config = new Configuration(service).withSampler(samplerConfig).withReporter(reporterConfig);
        return config.getTracer();
    }    
    private String formatString(Span root_span, String helloTo) {
    	Span span = tracer.buildSpan("formatString").asChildOf(root_span).start();
    	
        
        String helloStr = getHttp(8081, "format", "helloTo", helloTo);
        
        span.log(ImmutableMap.of("event", "format string", "anykey", helloStr));
        span.finish();
        return helloStr;
    }

    private void printHello(Span root_span, String helloStr) {
    	Span span = tracer.buildSpan("printHello").asChildOf(root_span).start();
        // log
        span.log(ImmutableMap.of("event", "begin println, and sleep 10s", "anykey", helloStr));
        System.out.println(helloStr);
        getHttp(8082, "publish", "helloStr", helloStr);
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
        Tracer tracer = initTracer("lesson3");
        new Hello(tracer).sayHello(helloTo);
    }
                    
}
