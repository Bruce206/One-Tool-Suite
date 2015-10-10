package de.bruss;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import de.bruss.settings.Settings;

public class EmfSingleton {

	protected EmfSingleton() {

	}

	static private EntityManagerFactory emf = null;

	static public EntityManagerFactory getEmfInstance() {
		if (null == emf) {
			emf = Persistence.createEntityManagerFactory(Settings.appDataPath + "\\database.odb");
		}
		return emf;
	}
}
