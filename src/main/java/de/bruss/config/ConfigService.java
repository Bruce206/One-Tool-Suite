package de.bruss.config;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import de.bruss.deployment.Category;
import org.apache.commons.beanutils.BeanUtils;

import de.bruss.EmfSingleton;
import de.bruss.deployment.Config;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigService {

    private static final Logger logger = LoggerFactory.getLogger(ConfigService.class);

    public static void addConfig(Config config) {
        EntityManager entityManager = EmfSingleton.getEmfInstance().createEntityManager();
        entityManager.getTransaction().begin();
        entityManager.persist(config);
        entityManager.getTransaction().commit();
        entityManager.close();
        logger.debug("Config created: " + config.getName());
    }

    public static Config getConfig(long id) {
        EntityManager em = EmfSingleton.getEmfInstance().createEntityManager();
        Config config = em.find(Config.class, id);
        return config;
    }

    public static List<Config> getAll() {
        EntityManager em = EmfSingleton.getEmfInstance().createEntityManager();

        TypedQuery<Config> query = em.createQuery("SELECT c FROM Config c", Config.class);

        List<Config> configs = query.getResultList();
        em.close();
        return configs;
    }

    public static List<Config> getAllWithoutCategory() {
        EntityManager em = EmfSingleton.getEmfInstance().createEntityManager();

        TypedQuery<Config> query = em.createQuery("SELECT c FROM Config c where c.category is null", Config.class);

        List<Config> configs = query.getResultList();
        em.close();
        return configs;
    }


    public static void save(Config config) {
        EntityManager entityManager = EmfSingleton.getEmfInstance().createEntityManager();
        Config dbConfig = entityManager.find(Config.class, config);

        entityManager.getTransaction().begin();
        try {
            BeanUtils.copyProperties(dbConfig, config);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        entityManager.getTransaction().commit();
        entityManager.close();
        logger.debug("Config saved: " + config.getName());
    }

    public static void remove(Config config) {
        if (config.getCategory() != null && config.getCategory().getId() != null) {
            Category cat = CategoryService.getCategory(config.getCategory().getId());
            cat.getConfigs().remove(config);
            CategoryService.save(cat);
        }

        EntityManager entityManager = EmfSingleton.getEmfInstance().createEntityManager();
        entityManager.getTransaction().begin();
        entityManager.remove(entityManager.find(Config.class, config.getId()));
        entityManager.getTransaction().commit();
        entityManager.close();
        logger.debug("Config removed: " + config.getName());
    }

}
