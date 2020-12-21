package app.editors.manager.managers.retrofit;

import com.google.gson.GsonBuilder;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Type;

import lib.toolkit.base.managers.utils.TimeUtils;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

public class ConverterFactory extends Converter.Factory {

    @Retention(RetentionPolicy.RUNTIME)
    public @interface Json {
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface Xml {
    }

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        for (Annotation annotation : annotations) {
            if (annotation.annotationType() == Xml.class) {
                return SimpleXmlConverterFactory.create().responseBodyConverter(type, annotations, retrofit);
            } else if (annotation.annotationType() == Json.class) {
                return getGsonConverter().responseBodyConverter(type, annotations, retrofit);
            }
        }
        return getGsonConverter().responseBodyConverter(type, annotations, retrofit);
    }

    @Override
    public Converter<?, RequestBody> requestBodyConverter(Type type, Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit) {
        for (Annotation annotation : methodAnnotations) {
            if (annotation.annotationType() == Xml.class) {
                return SimpleXmlConverterFactory.create().requestBodyConverter(type, parameterAnnotations, methodAnnotations, retrofit);
            } else if (annotation.annotationType() == Json.class) {
                return getGsonConverter().requestBodyConverter(type, parameterAnnotations, methodAnnotations, retrofit);
            }
        }
        return getGsonConverter().requestBodyConverter(type, parameterAnnotations, methodAnnotations, retrofit);
    }

    private Converter.Factory getGsonConverter() {
        return GsonConverterFactory.create(new GsonBuilder()
                .setLenient()
                .setPrettyPrinting()
                .setDateFormat(TimeUtils.OUTPUT_PATTERN_DEFAULT)
                .serializeNulls()
                .create());
    }
}