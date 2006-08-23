package org.dcm4chee.arr.ejb;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Example;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.ejb.RemoteBinding;

/**
 * A SLSB that has basic and generic CRUD functionalities for any entity bean
 * 
 * @author fyang
 * @version $id$
 * @since Aug 23, 2006 11:30:21 AM
 */
@Remote(GenericEntityMgmt.class)
@Local(GenericEntityMgmt.class)
@LocalBinding(jndiBinding = "dcm4chee/local/GenericEntityMgmt")
@RemoteBinding(jndiBinding = "dcm4chee/remote/GenericEntityMgmt")
public  @Stateless class GenericEntityMgmtBean implements GenericEntityMgmt {

	@PersistenceContext
	protected EntityManager em;

	protected @PersistenceContext
	Session session;

	public GenericEntityMgmtBean() {
	}

	public <T> T findByPk(Class<T> entityClass, Long pk) {
		return findByPk(entityClass, pk, false);
	}

	@SuppressWarnings("unchecked")
	public <T> T findByPk(Class<T> entityClass, Long pk, boolean lock) {
		T entity;
		if (lock) {
			entity = (T) ((org.hibernate.ejb.HibernateEntityManager) em).getSession().load(entityClass, pk,
					org.hibernate.LockMode.UPGRADE);
		} else {
			entity = (T) em.find(entityClass, pk);
		}
		return entity;
	}

	@SuppressWarnings("unchecked")
	public <T> List<T> findAll(Class<T> entityClass) {
		Query query = em.createQuery("from " + entityClass.getSimpleName());
		List<T> list = query.getResultList();

		return list;
	}

	@SuppressWarnings("unchecked")
	public <T> List<T> findByExample(Class<T> entityClass, T exampleInstance, String... excludeProperty) {
		// Using Hibernate, it's more difficult with EntityManager and EJB-QL
		Criteria crit = session.createCriteria(entityClass);
		Example example = Example.create(exampleInstance);
		for (String exclude : excludeProperty) {
			example.excludeProperty(exclude);
		}
		crit.add(example);
		return crit.list();
	}

	/**
	 * Check 2nd level cached entities
	 */
	@SuppressWarnings("unchecked")
	public <T> Collection<T> getCached(Class<T> entityClass) {
		Map entries = session.getSessionFactory().getStatistics().getSecondLevelCacheStatistics(
				entityClass.getName()).getEntries();
		for (Object o : entries.entrySet()) {
			Map.Entry e = (Map.Entry) o;
			System.out.println(e.getKey() + ": " + e.getValue());
		}
		return (Collection<T>) entries.values();
	}

	public Query createNamedQuery(String namedQuery) {
		return em.createNamedQuery(namedQuery);
	}

	public org.hibernate.Query createCachedNamedQuery(String cacheRegion, String namedQuery) {
		org.hibernate.Query query = session.getNamedQuery(namedQuery);
		query.setCacheable(true).setCacheRegion(cacheRegion);
		return query;
	}

	public Query createQuery(String query) {
		return em.createQuery(query);
	}

	public void merge(Object entity) {
		em.merge(entity);
	}

	/**
	 * Persist any entity in the context
	 */
	public void persistent(Object entity) {
		em.persist(entity);
	}

	public void flush() {
		em.flush();
	}

	public void remove(Object entity) {
		em.remove(entity);
	}

	/**
	 * General finder by any criteria related to the generic entity
	 */
	@SuppressWarnings("unchecked")
	protected <T> List<T> findByCriteria(Class<T> entityClass, org.hibernate.criterion.Criterion... criterion) {
		// Using Hibernate directly, since it's more difficult with EntityManager and EJB-QL
		org.hibernate.Session session = ((org.hibernate.ejb.HibernateEntityManager) em).getSession();
		org.hibernate.Criteria crit = session.createCriteria(entityClass);
		for (org.hibernate.criterion.Criterion c : criterion) {
			crit.add(c);
		}
		return crit.list();
	}
}
