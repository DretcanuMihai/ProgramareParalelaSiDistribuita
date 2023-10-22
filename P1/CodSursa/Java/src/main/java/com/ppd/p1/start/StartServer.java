package com.ppd.p1.start;

import com.ppd.p1.configuration.ServerConfiguration;
import com.ppd.p1.networking.Server;
import org.hibernate.SessionFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;


public class StartServer {
    public static void main(String[] args) {

        ApplicationContext context = new AnnotationConfigApplicationContext(ServerConfiguration.class);
        Server server = context.getBean(Server.class);
        server.start();
        SessionFactory sessionFactory = context.getBean(SessionFactory.class);
        sessionFactory.close();
    }
}
