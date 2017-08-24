package de.bruss.config;

import de.bruss.EmfSingleton;
import de.bruss.deployment.Category;
import de.bruss.deployment.Config;
import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class CategoryService {
    private static final Logger logger = LoggerFactory.getLogger(CategoryService.class);

    public static void addCategory(Category category) {
        EntityManager entityManager = EmfSingleton.getEmfInstance().createEntityManager();
        entityManager.getTransaction().begin();
        entityManager.persist(category);
        entityManager.getTransaction().commit();
        entityManager.close();
        logger.debug("Category created: " + category.getName());
    }

    public static Category getCategory(long id) {
        EntityManager em = EmfSingleton.getEmfInstance().createEntityManager();
        return em.find(Category.class, id);
    }

    public static List<Category> getAll() {
        EntityManager em = EmfSingleton.getEmfInstance().createEntityManager();

        TypedQuery<Category> query = em.createQuery("SELECT c FROM Category c", Category.class);

        List<Category> categories = query.getResultList();
        em.close();
        return categories;
    }

    public static void save(Category category) {
        EntityManager entityManager = EmfSingleton.getEmfInstance().createEntityManager();
        Category dbCategory = entityManager.find(Category.class, category.getId());

        entityManager.getTransaction().begin();
        try {
            BeanUtils.copyProperties(dbCategory, category);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        entityManager.getTransaction().commit();
        entityManager.close();
        logger.debug("Category saved: " + category.getName());
    }

    public static void remove(Category category) {
        for (Config config : category.getConfigs()) {
            config.setCategory(null);
            ConfigService.save(config);
        }

        EntityManager entityManager = EmfSingleton.getEmfInstance().createEntityManager();
        entityManager.getTransaction().begin();
        entityManager.remove(entityManager.find(Category.class, category.getId()));
        entityManager.getTransaction().commit();
        entityManager.close();
        logger.debug("Category removed: " + category.getName());
    }
}
