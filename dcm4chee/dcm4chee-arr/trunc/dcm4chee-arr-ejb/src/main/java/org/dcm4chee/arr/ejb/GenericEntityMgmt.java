package org.dcm4chee.arr.ejb;

import java.util.Collection;
import java.util.List;

/**
 * An interface that has generic and basic CRUD functionalities for any entity bean.
 * 
 * @author Fang Yang (fang.yang@agfa.com)
 * @version $Id$
 * @since Aug 23, 2006 11:30:21 AM
 */
public interface GenericEntityMgmt {

    <T> T findByPk(Class<T> entityClass, Integer pk);
    <T> T findByPk(Class<T> entityClass, Integer pk, boolean lock);
    <T> T findByPk(Class<T> entityClass, Long pk);
    <T> T findByPk(Class<T> entityClass, Long pk, boolean lock);
    <T> List<T> findAll(Class<T> entityClass);
    <T> List<T> findByExample(Class<T> entityClass, T exampleInstance, String... excludeProperty);
    
    <T> Collection<T> getCached(Class<T> entityClass);
    
    void persistent(Object entity);
    void merge(Object entity);
    void remove(Object entity);
    
    void flush();
    
    <T> List<T> query(Class<T> entityClass, String query, Object... paramValues);
    <T> List<T> queryByName(Class<T> entityClass, String namedQuery, Object... paramValues);    
    <T> List<T> cachedQueryByName(Class<T> entityClass, String cacheRegion, String namedQuery, Object... paramValues);        
}
