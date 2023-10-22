package com.ppd.p1.configuration;

import com.ppd.p1.networking.Server;
import com.ppd.p1.repository.*;
import com.ppd.p1.service.SuperService;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.*;

import static com.ppd.p1.commons.Constants.SERVER_PORT;

@Configuration
public class ServerConfiguration {

    private final String postgresUsername = "postgres";

    private final String postgresPassword = "postgres";

    @Bean
    public SessionFactory sessionFactory() {
        if (databaseExists()) {
            dropDatabase();
        }
        createDatabase();
        final StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .configure()
                .build();
        try {
            return new MetadataSources(registry).buildMetadata().buildSessionFactory();
        } catch (Exception e) {
            e.printStackTrace();
            StandardServiceRegistryBuilder.destroy(registry);
            throw new RuntimeException(e);
        }
    }

    @Bean
    public AppointmentRepository appointmentRepository() {
        return new AppointmentRepository(sessionFactory());
    }

    @Bean
    public LocationRepository locationRepository() {
        return new LocationRepository(sessionFactory());
    }

    @Bean
    public LocTreatOfferRepository locTreatOfferRepository() {
        return new LocTreatOfferRepository(sessionFactory());
    }

    @Bean
    public PaymentRepository paymentRepository() {
        return new PaymentRepository(sessionFactory());
    }

    @Bean
    public TreatmentRepository treatmentRepository() {
        return new TreatmentRepository(sessionFactory());
    }

    @Bean
    public SuperService superService() {
        return new SuperService(appointmentRepository(), locationRepository(), locTreatOfferRepository(),
                paymentRepository(), treatmentRepository());
    }

    @Bean
    public Server server() {
        return new Server(SERVER_PORT, superService());
    }

    private void dropDatabase() {
        try (Connection postgresConnection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/", postgresUsername, postgresPassword);
             Statement createDBStatement = postgresConnection.createStatement()) {

            createDBStatement.execute("""
                    DROP DATABASE p1
                    """);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void createDatabase() {
        try (Connection postgresConnection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/", postgresUsername, postgresPassword);
             Statement createDBStatement = postgresConnection.createStatement()) {

            createDBStatement.execute("""
                    CREATE DATABASE p1
                    """);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private boolean databaseExists() {
        boolean result;
        try (Connection c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/", postgresUsername, postgresPassword);
             Statement findDBStatement = c.createStatement()) {

            ResultSet resultSet = findDBStatement.executeQuery("""
                    SELECT datname FROM pg_catalog.pg_database WHERE datname='p1'
                    """);
            result = resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return result;
    }
}
