package com.olympus.olympus.framework.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

//@Configuration
//@EnableWebMvc
public class ResourceConfig implements WebMvcConfigurer {



    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry){


//        // https://www.webjars.org/ openui5 겸색새서 wejars 사용가능하도록 수정
//        // openui5를 webjar로 import하고 리소스에 연결 시킨다.
//        registry.addResourceHandler("/webjars/**").addResourceLocations("/webjars/").setCachePeriod(60 * 60 * 24);
//
//        //UI5 화면 파일을 리소스로 해야 클라인트 측에서 랜더링이 가능하다.
//        registry.addResourceHandler("/olympus/**").addResourceLocations("/olympus/").setCachePeriod(60);//60초 유지
//
//        //image를 resource 폴더에 넣어서 관리한다.
//        registry.addResourceHandler("/resources/**").addResourceLocations("/resources/").setCachePeriod(60 * 60 * 24);
        //
//        registry.addResourceHandler("/web/app/**").addResourceLocations("/").setCachePeriod(60);


//        /* '/css/**'로 호출하는 자원은 '/static/css/' 폴더 아래에서 찾는다. */
//        registry.addResourceHandler("/css/**").addResourceLocations("classpath:/static/css/").setCachePeriod(60 * 60 * 24 * 365);
//        /* '/img/**'로 호출하는 자원은 '/static/img/' 폴더 아래에서 찾는다. */
//        registry.addResourceHandler("/img/**").addResourceLocations("classpath:/static/img/").setCachePeriod(60 * 60 * 24 * 365);
//        /* '/font/**'로 호출하는 자원은 '/static/font/' 폴더 아래에서 찾는다. */
//        registry.addResourceHandler("/font/**").addResourceLocations("classpath:/static/font/").setCachePeriod(60 * 60 * 24 * 365);
    }

}

