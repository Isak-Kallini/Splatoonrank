import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import java.io.IOException;
import java.util.*;


public class Main {
    private static Calendar lastMatch;
    public static void main(String[] args) throws IOException {
        System.out.println(System.getProperty("user.dir"));
        StandardServiceRegistry ssr = new StandardServiceRegistryBuilder().configure("hibernate.cfg.xml").build();
        Metadata meta = new MetadataSources(ssr).getMetadataBuilder().build();
        SessionFactory factory = meta.getSessionFactoryBuilder().build();

        discordBot.Main.initialize(factory);
        //new discordBot.commands.Update().runNoDiscord();
    }
}
