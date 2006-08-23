package org.dcm4chee.arr.ejb;

import java.util.Collection;
import java.util.List;

import javax.persistence.Query;

/**
 * An interface that has generic and basic CRUD functionalities for any entity bean.
 * 
 * @author fang.yang@agfa.com
 * @version $Id$
 * @since Aug 23, 2006 11:30:21 AM
 */
public interface GenericEntityMgmt {

    <T> T findByPk(Class<T> entityClass, Long pk);
    <T> T findByPk(Class<T> entityClass, Long pk, boolean lock);
    <T> List<T> findAll(Class<T> entityClass);
    <T> List<T> findByExample(Class<T> entityClass, T exampleInstance, String... excludeProperty);
    
    <T> Collection<T> getCached(Class<T> entityClass);
    
    void persistent(Object entity);
    void merge(Object entity);
    void remove(Object entity);
    
    void flush();
    
    Query createNamedQuery(String namedQuery);    
    org.hibernate.Query createCachedNamedQuery(String cacheRegion, String namedQuery);        
    Query createQuery(String query);
}
