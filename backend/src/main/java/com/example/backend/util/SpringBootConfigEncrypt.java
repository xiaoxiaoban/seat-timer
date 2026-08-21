package com.example.backend.util;

import org.jasypt.encryption.StringEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class SpringBootConfigEncrypt implements CommandLineRunner {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private StringEncryptor encryptor;

    @Override
    public void run(String... args) throws Exception {

        Environment environment = applicationContext.getBean(Environment.class);

        // 首先获取配置文件里的原始明文信息 
        // 根据自己配置文件中的密码读取路径自行更改
        String oldPassword = environment.getProperty("spring.datasource.password");
        String oldUsername = environment.getProperty("spring.datasource.username");

        // 加密
        String encryptPassword = encrypt( oldPassword );
        String encryptUsername = encrypt( oldUsername );

        // 打印加密前后的结果对比
//        System.out.println( "MySQL原始明文密码为：" + oldPassword + "|" + oldUsername);
//        System.out.println( "====================================" );
//        System.out.println( "MySQL原始明文密码加密后的结果为：" + encryptPassword + "|" + encryptUsername);

    }


    private String encrypt( String originPassord ) {
        return encryptor.encrypt( originPassord );
    }

    private String decrypt( String encryptedPassword ) {
        return encryptor.decrypt( encryptedPassword );
    }
}
