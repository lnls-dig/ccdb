/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openepics.discs.conf.ejb;

import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.openepics.discs.conf.ent.AuditRecord;
import org.openepics.discs.conf.ent.InstallationArtifact;
import org.openepics.discs.conf.ent.InstallationRecord;
import org.openepics.discs.conf.ent.Slot;
import org.openepics.discs.conf.ui.LoginManager;
import org.openepics.discs.conf.util.AppProperties;

/**
 *
 * @author vuppala
 */
@Stateless
public class InstallationEJB implements InstallationEJBLocal {

    private static final Logger logger = Logger.getLogger("org.openepics.discs.conf");   
    @PersistenceContext(unitName = "org.openepics.discs.conf.data")
    private EntityManager em;
    // Add business logic below. (Right-click in editor and choose
    // "Insert Code > Add Business Method")
    @EJB
    private AuthEJBLocal authEJB;
    
    @Inject 
    private LoginManager loginManager;
    
    // ----------- Audit record ---------------------------------------
    private void makeAuditEntry(char oper, String key, String entry) {
        AuditRecord arec = new AuditRecord(null, new Date(), oper, loginManager.getUserid(), entry);
        arec.setEntityType(AppProperties.EN_INSTALLREC);
        arec.setEntityKey(key);
        em.persist(arec);
    }
    
    // ----- Installation 
    @Override
    public List<InstallationRecord> findInstallationRec() {
        List<InstallationRecord> irecs;
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<InstallationRecord> cq = cb.createQuery(InstallationRecord.class);
        Root<InstallationRecord> prop = cq.from(InstallationRecord.class);       
        
        TypedQuery<InstallationRecord> query = em.createQuery(cq);
        irecs = query.getResultList();
        logger.log(Level.INFO, "Number of physical components: {0}", irecs.size());
        
        return irecs;        
    }
    
    @Override
    public void saveIRecord(InstallationRecord irec, boolean create) throws Exception  {
        if (irec == null) {
            logger.log(Level.SEVERE, "Installation Record is null!");
            return;
            // throw new Exception("property is null");        
        }
        String user = loginManager.getUserid();
        if (! authEJB.userHasAuth(user, AppProperties.EN_INSTALLREC, AppProperties.OPER_UPDATE)) {
            logger.log(Level.SEVERE, "User is not authorized to perform this operation:  " + user);
            throw new Exception("User " + user + " is not authorized to perform this operation");
        }
        irec.setModifiedAt(new Date());

        logger.log(Level.INFO, "Preparing to save installation record");
        
        if ( irec.getInstallationRecordId() == null ) { // new record
            em.persist(irec);
            Slot slot = irec.getSlot();
            slot.getInstallationRecordList().add(irec);
            em.merge(slot);
            // todo: add to the list for the device
        } else {
            em.merge(irec);
        }
        makeAuditEntry(AppProperties.OPER_UPDATE,irec.getRecordNumber(),"updated installation record ");
    }
    
    @Override
    public void deleteIRecord(InstallationRecord irec) throws Exception {
        if (irec == null ) {
            logger.log(Level.SEVERE, "Installation Record is null!");
            throw new Exception("Installation Record is null");        
        }
        String user = loginManager.getUserid();
        if (! authEJB.userHasAuth(user, AppProperties.EN_INSTALLREC, AppProperties.OPER_DELETE)) {
            logger.log(Level.SEVERE, "User is not authorized to perform this operation:  " + user);
            throw new Exception("User " + user + " is not authorized to perform this operation");
        }
        InstallationRecord ct = em.find(InstallationRecord.class,irec.getInstallationRecordId());
        em.remove(ct);
        makeAuditEntry(AppProperties.OPER_DELETE,irec.getRecordNumber(),"deleted installation record ");
    }
    
    // ---------------- Artifact ---------------------
    
    @Override
    public void saveInstallationArtifact(InstallationArtifact art, boolean create) throws Exception {
        if (art == null) {
            logger.log(Level.SEVERE, "deleteInstallationArtifact: Installation Record is null");
            return;
        }
        String user = loginManager.getUserid();
        if (! authEJB.userHasAuth(user, AppProperties.EN_INSTALLREC, AppProperties.OPER_UPDATE)) {
            logger.log(Level.SEVERE, "User is not authorized to perform this operation:  " + user);
            throw new Exception("User " + user + " is not authorized to perform this operation");
        }
        art.setModifiedAt(new Date());
        art.setModifiedBy("user");
        InstallationArtifact newArt = em.merge(art);
        if (create) { // create instead of update
            InstallationRecord arec = art.getInstallationRecord();           
            arec.getInstallationArtifactList().add(newArt);
            em.merge(arec);
        }
        makeAuditEntry(AppProperties.OPER_UPDATE,art.getInstallationRecord().getRecordNumber(),"updated installation artifact " + art.getName());
        logger.log(Level.INFO, "Artifact: name " + art.getName() + " description " + art.getDescription() + " uri " + art.getUri() + "is int " + art.getIsInternal());
        // logger.log(Level.INFO, "device serial " + device.getSerialNumber());
    }
    
    @Override
    public void deleteInstallationArtifact(InstallationArtifact art) throws Exception {
        if (art == null) {
            logger.log(Level.SEVERE, "deleteInstallationArtifact: irec-artifact is null");
            return;
        }
        String user = loginManager.getUserid();
        if (! authEJB.userHasAuth(user, AppProperties.EN_INSTALLREC, AppProperties.OPER_UPDATE)) {
            logger.log(Level.SEVERE, "User is not authorized to perform this operation:  " + user);
            throw new Exception("User " + user + " is not authorized to perform this operation");
        }
        logger.log(Level.INFO, "deleting " + art.getName() + " id " + art.getArtifactId() + " des " + art.getDescription());
        InstallationArtifact artifact = em.find(InstallationArtifact.class, art.getArtifactId());
        InstallationRecord irec = artifact.getInstallationRecord();
        
        em.remove(artifact);
        irec.getInstallationArtifactList().remove(artifact);
        makeAuditEntry(AppProperties.OPER_UPDATE,art.getInstallationRecord().getRecordNumber(),"deleted installation artifact " + art.getName());
    }
}
