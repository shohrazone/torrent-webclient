package com.torrent.webclient.httpclient;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class HttpClientInitializer {
//    private final HttpClientInterceptor httpClientAspect;
//    private ConfigurableBeanFactory beanFactory;
//
//    @Override
//    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
//        this.beanFactory = (ConfigurableBeanFactory) beanFactory;
//    }
//
//    @PostConstruct
//    public void initializeHttpClients() {
//        Set<Class<?>> typesAnnotatedWithHttpClient = new Reflections("com.torrent.webclient").getTypesAnnotatedWith(HttpClient.class);
//
//        typesAnnotatedWithHttpClient
//                .forEach(eachHttpClient -> {
//                    Class[] interfaces = new Class[]{eachHttpClient};
//                    ClassLoader loader = HttpClientInitializer.class.getClassLoader();
//                    Object proxyInstance = Proxy.newProxyInstance(loader, interfaces, httpClientAspect);
//                    beanFactory.registerSingleton(eachHttpClient.getName(), proxyInstance);
//                });
//    }
}
