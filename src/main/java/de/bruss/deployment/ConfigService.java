package de.bruss.deployment;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.commons.beanutils.BeanUtils;

import de.bruss.EmfSingleton;

public class ConfigService {

	public static void addConfig(Config config) {
		EntityManager entityManager = EmfSingleton.getEmfInstance().createEntityManager();
		entityManager.getTransaction().begin();
		entityManager.persist(config);
		entityManager.getTransaction().commit();
		entityManager.close();
		System.out.println("Config saved!");
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
		System.out.println("Config saved!");
	}

	public static void remove(Config config) {
		EntityManager entityManager = EmfSingleton.getEmfInstance().createEntityManager();
		entityManager.getTransaction().begin();
		entityManager.remove(entityManager.find(Config.class, config.getId()));
		entityManager.getTransaction().commit();
		entityManager.close();
		System.out.println("Config removed!");
	}

}
