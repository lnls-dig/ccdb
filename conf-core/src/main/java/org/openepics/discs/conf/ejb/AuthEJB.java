/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openepics.discs.conf.ejb;

import java.util.List;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import org.openepics.discs.conf.ent.Privilege;
import org.openepics.discs.conf.ent.UserRole;

/**
 *
 * @author vuppala
 */
@Stateless
public class AuthEJB implements AuthEJBLocal {

    private static final Logger logger = Logger.getLogger("org.openepics.discs.conf");
    @PersistenceContext(unitName = "org.openepics.discs.conf.data")
    private EntityManager em;
    // Add business logic below. (Right-click in editor and choose
    // "Insert Code > Add Business Method")

    // todo: need to implement autorization. this is just a dummy   
    @Override
    public boolean userHasAuth(String principal, String resource, char operation) {
        boolean auth = false;

        if (principal == null || principal.isEmpty()) {
            return false;
        }

        List<Privilege> privs;
        TypedQuery<Privilege> query;
        //query = em.createQuery("SELECT p FROM UserRole ur JOIN ur.role r JOIN r.privilegeList p WHERE ur.user.userId = :user AND LOCATE(:resource,p.resource) = 1", Privilege.class)
        query = em.createQuery("SELECT p FROM UserRole ur JOIN ur.role r JOIN r.privilegeList p WHERE ur.user.userId = :user", Privilege.class)
                .setParameter("user", principal);

        privs = query.getResultList();
        logger.info("AuthEJB: found privileges: " + privs.size());

        for (Privilege p : privs) {
            if (resource.matches(p.getResource())) {
                logger.info("AuthEJB: matched privileges: " + p);
                if (p.getOper() == '.' || p.getOper() == operation) {
                    auth = true;
                    break;
                }
            }
        }
        return auth;
    }
}
