package org.example;

import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

public class HibernateHelper {
    private static StandardServiceRegistry registry;
    private static SessionFactory sessionFactory;

    public HibernateHelper() {
        throw new IllegalStateException("Utility classes cannot be instantiated");
    }


    public static SessionFactory getSessionFactory() {
        if(sessionFactory == null) {
            try{
                registry = new StandardServiceRegistryBuilder().configure().build();

                final MetadataSources metadataSources = new MetadataSources(registry);
                metadataSources.addAnnotatedClass(UrlService.class);

                final Metadata metadata = metadataSources.getMetadataBuilder().build();

                sessionFactory = metadata.getSessionFactoryBuilder().build();
            }catch(Exception e) {
                e.printStackTrace();
                shutdown();
            }
        }
        return sessionFactory;
    }

    private static void shutdown() {
        if(registry != null) {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }

}