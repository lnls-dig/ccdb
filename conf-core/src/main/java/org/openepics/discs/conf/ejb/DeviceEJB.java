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
import org.openepics.discs.conf.ent.Device;
import org.openepics.discs.conf.ent.DeviceArtifact;
import org.openepics.discs.conf.ent.DeviceProperty;
import org.openepics.discs.conf.ui.LoginManager;
import org.openepics.discs.conf.util.AppProperties;

/**
 *
 * @author vuppala
 */
@Stateless
public class DeviceEJB implements DeviceEJBLocal {
    @EJB
    private AuthEJBLocal authEJB;
    
    @Inject 
    private LoginManager loginManager;
    
    private static final Logger logger = Logger.getLogger("org.openepics.discs.conf");   
    @PersistenceContext(unitName = "org.openepics.discs.conf.data")
    private EntityManager em;
    
    
    // Add business logic below. (Right-click in editor and choose
    // "Insert Code > Add Business Method")
    
    // ----------- Audit record ---------------------------------------
    private void makeAuditEntry(char oper, String key, String entry) {
        AuditRecord arec = new AuditRecord(null, new Date(), oper, loginManager.getUserid(), entry);
        arec.setEntityType(AppProperties.EN_DEVICE);
        arec.setEntityKey(key);
        em.persist(arec);
    }
    
    // ----------------  Physical Component  -------------------------
    @Override
    public List<Device> findDevice() {
        List<Device> comps;
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Device> cq = cb.createQuery(Device.class);
        Root<Device> prop = cq.from(Device.class);       
        
        TypedQuery<Device> query = em.createQuery(cq);
        comps = query.getResultList();
        logger.log(Level.INFO, "Number of devices: {0}", comps.size());
        
        return comps;   
    }
    
    @Override
    public Device findDevice(int id) {
        return em.find(Device.class, id);
    }
    
    @Override
    public void saveDevice(String token, Device device) throws Exception {
        String user = token; // todo: convert token to user
        
        if (device == null ) {
            logger.log(Level.SEVERE, "Device is null!");
            return;
            // throw new Exception("property is null");        
        }
        if (! authEJB.userHasAuth(user, AppProperties.EN_DEVICE, AppProperties.OPER_CREATE)) {
            logger.log(Level.SEVERE, "User is not authorized to perform this operation:  " + user);
            throw new Exception("User " + user + " is not authorized to perform this operation");
        }
        
        device.setModifiedAt(new Date()); 
        device.setModifiedBy(user);
        logger.log(Level.INFO, "Preparing to save device");
        em.merge(device);
        makeAuditEntry(AppProperties.OPER_UPDATE,device.getSerialNumber(),"Modified device");
    }
    
    @Override
    public void deleteDevice(Device device) throws Exception {
        
        if (device == null ) {
            logger.log(Level.SEVERE, "Property is null!");
            throw new Exception("property is null");        
        }
        String user = loginManager.getUserid();
        if (! authEJB.userHasAuth(user, AppProperties.EN_DEVICE, AppProperties.OPER_DELETE)) {
            logger.log(Level.SEVERE, "User is not authorized to perform this operation:  " + user);
            throw new Exception("User " + user + " is not authorized to perform this operation");
        }
        Device ct = em.find(Device.class,device.getDeviceId());
        em.remove(ct);     
        makeAuditEntry(AppProperties.OPER_DELETE,device.getSerialNumber(),"Deleted device");
    }
    
    // ------------------ Property ---------------
    @Override
    public void saveDeviceProp(DeviceProperty prop, boolean create) throws Exception {
        if (prop == null) {
            logger.log(Level.SEVERE, "saveDeviceProp: property is null");
            return;
        }
        String user = loginManager.getUserid();
        if (! authEJB.userHasAuth(user, AppProperties.EN_DEVICE, AppProperties.OPER_UPDATE)) {
            logger.log(Level.SEVERE, "User is not authorized to perform this operation:  " + user);
            throw new Exception("User " + user + " is not authorized to perform this operation");
        }
        prop.setModifiedAt(new Date());
        // ctprop.setType("a");
        prop.setModifiedBy(user);
        DeviceProperty newProp = em.merge(prop);

        if (create) { // create instead of update
            Device device = prop.getDevice();
            device.getDevicePropertyList().add(newProp);
            em.merge(device);
        }       
        logger.log(Level.INFO, "Comp Type Property: id " + newProp.getDevPropId() + " name " + newProp.getProperty().getName());
        makeAuditEntry(AppProperties.OPER_UPDATE, prop.getDevice().getSerialNumber(),"Modified property " + prop.getProperty().getName());
    }
    
    @Override
    public void deleteDeviceProp(DeviceProperty prop) throws Exception {
        if (prop == null) {
            logger.log(Level.SEVERE, "deleteDeviceArtifact: dev-artifact is null");
            return;
        }
        String user = loginManager.getUserid();
        if (! authEJB.userHasAuth(user, AppProperties.EN_DEVICE, AppProperties.OPER_UPDATE)) {
            logger.log(Level.SEVERE, "User is not authorized to perform this operation:  " + user);
            throw new Exception("User " + user + " is not authorized to perform this operation");
        }
        DeviceProperty property = em.find(DeviceProperty.class, prop.getDevPropId());
        Device device = property.getDevice();
        device.getDevicePropertyList().remove(property);
        em.remove(property);
        makeAuditEntry(AppProperties.OPER_UPDATE, prop.getDevice().getSerialNumber(),"Deleted property " + prop.getProperty().getName());
    } 
    
    // ---------------- Artifact ---------------------
    
    @Override
    public void saveDeviceArtifact(DeviceArtifact art, boolean create) throws Exception {
        if (art == null) {
            logger.log(Level.SEVERE, "deleteDeviceArtifact: Device is null");
            return;
        }
        String user = loginManager.getUserid();
        if (! authEJB.userHasAuth(user, AppProperties.EN_DEVICE, AppProperties.OPER_UPDATE)) {
            logger.log(Level.SEVERE, "User is not authorized to perform this operation:  " + user);
            throw new Exception("User " + user + " is not authorized to perform this operation");
        }
        art.setModifiedAt(new Date());
        art.setModifiedBy("user");
        DeviceArtifact newArt = em.merge(art);
        if (create) { // create instead of update
            Device dev = art.getDevice();           
            dev.getDeviceArtifactList().add(newArt);
            em.merge(dev);
        }
        // logger.log(Level.INFO, "Artifact: name " + art.getName() + " description " + art.getDescription() + " uri " + art.getUri() + "is int " + art.getIsInternal());
        // logger.log(Level.INFO, "device serial " + device.getSerialNumber());
       makeAuditEntry(AppProperties.OPER_UPDATE, art.getDevice().getSerialNumber(),"Modified artifact " + art.getName());
    }
    
    @Override
    public void deleteDeviceArtifact(DeviceArtifact art) throws Exception {
        if (art == null) {
            logger.log(Level.SEVERE, "deleteDeviceArtifact: dev-artifact is null");
            return;
        }
        String user = loginManager.getUserid();
        if (! authEJB.userHasAuth(user, AppProperties.EN_DEVICE, AppProperties.OPER_UPDATE)) {
            logger.log(Level.SEVERE, "User is not authorized to perform this operation:  " + user);
            throw new Exception("User " + user + " is not authorized to perform this operation");
        }
        logger.log(Level.INFO, "deleting " + art.getName() + " id " + art.getArtifactId() + " des " + art.getDescription());
        DeviceArtifact artifact = em.find(DeviceArtifact.class, art.getArtifactId());
        Device device = artifact.getDevice();
        device.getDeviceArtifactList().remove(artifact);
        em.remove(artifact); 
        makeAuditEntry(AppProperties.OPER_UPDATE, art.getDevice().getSerialNumber(),"Deleted artifact " + art.getName());
    }
}
