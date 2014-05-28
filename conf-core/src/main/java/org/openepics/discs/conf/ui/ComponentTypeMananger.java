/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openepics.discs.conf.ui;

import org.openepics.discs.conf.util.Utility;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.openepics.discs.conf.ejb.ComptypeEJBLocal;
import org.openepics.discs.conf.ent.ComptypeAsm;
import org.openepics.discs.conf.ent.ComponentType;
import org.openepics.discs.conf.ent.ComptypeProperty;
import org.openepics.discs.conf.ent.ComptypeArtifact;
import org.openepics.discs.conf.util.BlobStore;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.RowEditEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;

/**
 *
 * @author vuppala
 */
@Named
@ViewScoped
public class ComponentTypeMananger implements Serializable {

    @EJB
    private ComptypeEJBLocal comptypeEJB;
    private static final Logger logger = Logger.getLogger("org.openepics.discs.conf");
    // private static String folderName = "/var/proteus/"; // ToDo: get it from configuration

    @Inject
    private BlobStore blobStore;
    
    private List<ComponentType> objects;
    private List<ComponentType> sortedObjects;
    private List<ComponentType> filteredObjects;
    private ComponentType selectedObject;
    private ComponentType inputObject;
    private List<ComptypeAsm> selectedParts;

    // properties
    private List<ComptypeProperty> selectedProperties;
    private ComptypeProperty selectedProperty;
    private ComptypeProperty inputProperty;
    private boolean inRepository = false;
    private char propertyOperation = 'n'; // selected operation on artifact: [a]dd, [e]dit, [d]elete, [n]one

    // artifacts
    private List<ComptypeArtifact> selectedArtifacts;
    private ComptypeArtifact inputArtifact;
    private boolean internalArtifact = true;
    private ComptypeArtifact selectedArtifact;
    private char artifactOperation = 'n'; // selected operation on artifact: [a]dd, [e]dit, [d]elete, [n]one

    // File upload/download
    private String uploadedFileName;
    private boolean fileUploaded = false;
    private String repoFileId; // identifier of the file stored in content repo 

    private boolean inTrans = false; // in the middle of an operations  
    private char selectedOp = 'n'; // selected operation: [a]dd, [e]dit, [d]elete, [n]one

    /**
     * Creates a new instance of ComponentTypeMananger
     */
    public ComponentTypeMananger() {
    }

    @PostConstruct
    public void init() {
        try {
            objects = comptypeEJB.findComponentType();
            logger.log(Level.INFO, "Property org.openepics.discs.conf.prop.RepoPath {0}", blobStore.getBlobStoreRoot());
        } catch (Exception e) {
            System.err.println(e.getMessage());
            logger.log(Level.SEVERE, "Cannot retrieve component types");
            Utility.showMessage(FacesMessage.SEVERITY_INFO, "Error in getting component types", " ");
        }
    }

    // ----------------- Component Type ------------------------------
    public void onCompTypeSelect(SelectEvent event) {
        inputObject = selectedObject;

        selectedProperties = selectedObject.getComptypePropertyList();
        selectedArtifacts = selectedObject.getComptypeArtifactList();
        selectedParts = selectedObject.getComptypeAsmList();
        Utility.showMessage(FacesMessage.SEVERITY_INFO, "Selected", "");
    }

    public void onCompTypeAdd(ActionEvent event) {
        selectedOp = 'a';
        inTrans = true;

        inputObject = new ComponentType();
        Utility.showMessage(FacesMessage.SEVERITY_INFO, "Add", "");
    }

    public void onCompTypeEdit(ActionEvent event) {
        selectedOp = 'e';
        inTrans = true;
        inputObject = selectedObject;
        Utility.showMessage(FacesMessage.SEVERITY_INFO, "Edit", "");
    }

    public void onCompTypeDelete(ActionEvent event) {
        try {
            comptypeEJB.deleteComponentType(selectedObject);
            objects.remove(selectedObject);
            selectedObject = null;
            inputObject = null;
            Utility.showMessage(FacesMessage.SEVERITY_INFO, "Deleted", "");
        } catch (Exception e) {
            Utility.showMessage(FacesMessage.SEVERITY_ERROR, "Deleted", e.getMessage());
        } finally {

        }
    }

    public void onCompTypeSave(ActionEvent event) {
        try {
            // inputObject.setAssociation("T");
            inputObject.setModifiedBy("test-user");
            // inputObject.setSuperComponentType(null);
            // Utility.showMessage(FacesMessage.SEVERITY_INFO, "Saved 2", "");
            comptypeEJB.saveComponentType(inputObject);

            if (selectedOp == 'a') {
                selectedObject = inputObject;
                objects.add(selectedObject);
            }
            Utility.showMessage(FacesMessage.SEVERITY_INFO, "Saved", "");
        } catch (Exception e) {
            logger.severe(e.getMessage());
            Utility.showMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage());
        } finally {
            inTrans = false;
            selectedOp = 'n';
        }
    }

    // --------------------------------- Property ------------------------------------------------
    public void onPropertyAdd(ActionEvent event) {
        try {
            if (selectedProperties == null) {
                selectedProperties = new ArrayList<>();
            }
            propertyOperation = 'a';

            inputProperty = new ComptypeProperty();
            inputProperty.setComponentType(selectedObject);
            fileUploaded = false;
            uploadedFileName = null;
            Utility.showMessage(FacesMessage.SEVERITY_INFO, "New property", "");
        } catch (Exception e) {
            Utility.showMessage(FacesMessage.SEVERITY_ERROR, "Error in adding property", e.getMessage());
            logger.severe(e.getMessage());
        }

    }

    public void onPropertyDelete(ComptypeProperty ctp) {
        try {
            if (ctp == null) {
                Utility.showMessage(FacesMessage.SEVERITY_INFO, "Strange", "No property selected");
                return;
            }
            comptypeEJB.deleteCompTypeProp(ctp);
            selectedProperties.remove(ctp);
            Utility.showMessage(FacesMessage.SEVERITY_INFO, "Deleted property", "");
        } catch (Exception e) {
            Utility.showMessage(FacesMessage.SEVERITY_ERROR, "Error in deleting property", e.getMessage());
            logger.severe(e.getMessage());
        }

    }

    public void onPropertyEdit(ComptypeProperty prop) {
        try {
            if (prop == null) {
                Utility.showMessage(FacesMessage.SEVERITY_INFO, "Strange", "No property selected");
                return;
            }
            artifactOperation = 'e';
            inputProperty = prop;
            uploadedFileName = prop.getProperty().getName();
            Utility.showMessage(FacesMessage.SEVERITY_INFO, "Edit:", "Edit initiated " + inputProperty.getCtypePropId());
        } catch (Exception e) {
            // selectedCompProps.remove(prop);
            Utility.showMessage(FacesMessage.SEVERITY_ERROR, "Error: Property can not be edited", e.getMessage());
        }
    }

    public void onPropertySave(ActionEvent event) {
        // ComptypeProperty ctp = (ComptypeProperty) event.getObject();
        try {

            inputProperty.setInRepository(inRepository);
            if (inRepository) { // internal artifact
                if (!fileUploaded) {
                    Utility.showMessage(FacesMessage.SEVERITY_ERROR, "Error:", "You must upload a file");
                    RequestContext.getCurrentInstance().addCallbackParam("success", false);
                    return;
                }
                inputProperty.setPropValue(repoFileId);
            }

            comptypeEJB.saveCompTypeProp(inputProperty, propertyOperation == 'a');
            logger.log(Level.INFO, "returned artifact id is " + inputProperty.getCtypePropId());

            Utility.showMessage(FacesMessage.SEVERITY_INFO, "Property saved", "");
            RequestContext.getCurrentInstance().addCallbackParam("success", true);
        } catch (Exception e) {

            Utility.showMessage(FacesMessage.SEVERITY_ERROR, "Error: Property not saved", e.getMessage());
            RequestContext.getCurrentInstance().addCallbackParam("success", false);
        }
    }

    // -------------------------- File upload/download Property ---------------------------
    // todo: merge with artifact file ops. finally put in blobStore
    public void handlePropertyUpload(FileUploadEvent event) {
        // String msg = event.getFile().getFileName() + " is uploaded.";
        // Utility.showMessage(FacesMessage.SEVERITY_INFO, "Succesful", msg);
        InputStream istream;

        try {
            UploadedFile uploadedFile = event.getFile();
            uploadedFileName = uploadedFile.getFileName();
            // inputArtifact.setName(uploadedFileName);
            istream = uploadedFile.getInputstream();

            Utility.showMessage(FacesMessage.SEVERITY_INFO, "File ", "Name: " + uploadedFileName);
            repoFileId = blobStore.storeFile(istream);
            // inputArtifact.setUri(fileId);

            istream.close();
            Utility.showMessage(FacesMessage.SEVERITY_INFO, "File uploaded", "Name: " + uploadedFileName);
            fileUploaded = true;
        } catch (Exception e) {
            Utility.showMessage(FacesMessage.SEVERITY_ERROR, "Error", "Uploading file");
            logger.severe(e.getMessage());
            fileUploaded = false;
        } finally {

        }
    }

    public StreamedContent getDownloadedPropertyFile() {
        StreamedContent file = null;

        try {
            // return downloadedFile;
            logger.log(Level.INFO, "Opening stream from repository: " + selectedProperty.getPropValue());
            // logger.log(Level.INFO, "download file name: 2 " + selectedProperty.getName());
            InputStream istream = blobStore.retreiveFile(selectedProperty.getPropValue());
            file = new DefaultStreamedContent(istream, "application/octet-stream", selectedProperty.getProperty().getName());

            // InputStream stream = new FileInputStream(pathName);                       
            // downloadedFile = new DefaultStreamedContent(stream, "application/octet-stream", "file.jpg"); //ToDo" replace with actual filename
        } catch (Exception e) {
            Utility.showMessage(FacesMessage.SEVERITY_ERROR, "Error", "Downloading file");
            logger.log(Level.SEVERE, "Error in downloading the file");
            logger.log(Level.SEVERE, e.toString());
        }

        return file;
    }

    // --------------------------------- Artifact ------------------------------------------------
    public void onArtifactAdd(ActionEvent event) {
        try {
            artifactOperation = 'a';
            if (selectedArtifacts == null) {
                selectedArtifacts = new ArrayList<>();
            }
            inputArtifact = new ComptypeArtifact();
            inputArtifact.setComponentType(selectedObject);
            fileUploaded = false;
            uploadedFileName = null;
            Utility.showMessage(FacesMessage.SEVERITY_INFO, "New artifact", "");
        } catch (Exception e) {
            Utility.showMessage(FacesMessage.SEVERITY_ERROR, "Error in adding artifact",e.getMessage());
            logger.severe(e.getMessage());
        }
    }

    public void onArtifactSave(ActionEvent event) {
        try {
            if (artifactOperation == 'a') {
                inputArtifact.setIsInternal(internalArtifact);
                if (inputArtifact.getIsInternal()) { // internal artifact
                    if (!fileUploaded) {
                        Utility.showMessage(FacesMessage.SEVERITY_ERROR, "Error:", "You must upload a file");
                        RequestContext.getCurrentInstance().addCallbackParam("success", false);
                        return;
                    }
                }
            }

            // comptypeEJB.saveComponentTypeArtifact(selectedObject, inputArtifact);
            comptypeEJB.saveCompTypeArtifact(inputArtifact, artifactOperation == 'a');
            logger.log(Level.INFO, "returned artifact id is " + inputArtifact.getArtifactId());

            Utility.showMessage(FacesMessage.SEVERITY_INFO, "Artifact saved", "");
            RequestContext.getCurrentInstance().addCallbackParam("success", true);
        } catch (Exception e) {
            // selectedArtifacts.remove(inputArtifact);
            Utility.showMessage(FacesMessage.SEVERITY_ERROR, "Error: Artifact not saved", e.getMessage());
            RequestContext.getCurrentInstance().addCallbackParam("success", false);
        }
    }

    public void onArtifactDelete(ComptypeArtifact art) {
        try {
            if (art == null) {
                Utility.showMessage(FacesMessage.SEVERITY_INFO, "Strange", "No artifact selected");
                return;
            }

            comptypeEJB.deleteCompTypeArtifact(art);
            selectedArtifacts.remove(art);
            Utility.showMessage(FacesMessage.SEVERITY_INFO, "Deleted Artifact", "");
        } catch (Exception e) {
            Utility.showMessage(FacesMessage.SEVERITY_FATAL, "Error in deleting artifact", "Refresh the page");
            logger.severe(e.getMessage());
        }
    }

    public void onArtifactEdit(ComptypeArtifact art) {
        if (art == null) {
            Utility.showMessage(FacesMessage.SEVERITY_INFO, "Strange", "No artifact selected");
            return;
        }
        artifactOperation = 'e';
        inputArtifact = art;
        uploadedFileName = art.getName();
        Utility.showMessage(FacesMessage.SEVERITY_INFO, "Edit:", "Edit initiated " + inputArtifact.getArtifactId());
    }

    public void onArtifactType() {
        // Toto: remove it
        Utility.showMessage(FacesMessage.SEVERITY_INFO, "Artifact type selected", "");
    }

    // -------------------------- File upload/download Artifact ---------------------------
    public void handleFileUpload(FileUploadEvent event) {
        // String msg = event.getFile().getFileName() + " is uploaded.";
        // Utility.showMessage(FacesMessage.SEVERITY_INFO, "Succesful", msg);
        InputStream istream;

        try {
            UploadedFile uploadedFile = event.getFile();
            uploadedFileName = uploadedFile.getFileName();
            inputArtifact.setName(uploadedFileName);
            istream = uploadedFile.getInputstream();

            Utility.showMessage(FacesMessage.SEVERITY_INFO, "File ", "Name: " + uploadedFileName);
            String fileId = blobStore.storeFile(istream);
            inputArtifact.setUri(fileId);

            istream.close();
            Utility.showMessage(FacesMessage.SEVERITY_INFO, "File uploaded", "Name: " + uploadedFileName);
            fileUploaded = true;
        } catch (Exception e) {
            Utility.showMessage(FacesMessage.SEVERITY_ERROR, "Error Uploading file", e.getMessage());
            logger.severe(e.getMessage());
            fileUploaded = false;
        } finally {

        }
    }

    public StreamedContent getDownloadedFile() {
        StreamedContent file = null;

        try {
            // return downloadedFile;
            logger.log(Level.INFO, "Opening stream from repository: " + selectedArtifact.getUri());
            logger.log(Level.INFO, "download file name: 2 " + selectedArtifact.getName());
            InputStream istream = blobStore.retreiveFile(selectedArtifact.getUri());
            file = new DefaultStreamedContent(istream, "application/octet-stream", selectedArtifact.getName());

            // InputStream stream = new FileInputStream(pathName);                       
            // downloadedFile = new DefaultStreamedContent(stream, "application/octet-stream", "file.jpg"); //ToDo" replace with actual filename
        } catch (Exception e) {
            Utility.showMessage(FacesMessage.SEVERITY_ERROR, "Error Downloading file", e.getMessage());
            logger.log(Level.SEVERE, "Error in downloading the file");
            logger.log(Level.SEVERE, e.toString());
        }

        return file;
    }

    // --------------------------------- Assembly ------------------------------------------------
    public void onAsmAdd(ActionEvent event) {
        try {
            if (selectedParts == null) {
                selectedParts = new ArrayList<>();
            }
          
            ComptypeAsm prt = new ComptypeAsm();

            prt.setParentType(selectedObject);
            // CTP.setComponentType1(inputObject);
            selectedParts.add(prt);
            Utility.showMessage(FacesMessage.SEVERITY_INFO, "New assembly element", "");
        } catch (Exception e) {
            Utility.showMessage(FacesMessage.SEVERITY_ERROR, "Error in adding assembly element", e.getMessage());
            logger.severe(e.getMessage());
        }

    }

    public void onAsmDelete(ComptypeAsm prt) {
        try {
            if (prt == null) {
                Utility.showMessage(FacesMessage.SEVERITY_INFO, "Strange", "No assembly element selected");
                return;
            }
            selectedParts.remove(prt); // ToDo: should this be done before or after delete from db?
            comptypeEJB.deleteComptypeAsm(selectedObject, prt);

            Utility.showMessage(FacesMessage.SEVERITY_INFO, "Deleted assembly element", "");
        } catch (Exception e) {
            Utility.showMessage(FacesMessage.SEVERITY_ERROR, "Error in deleting assembly element", e.getMessage());
            logger.severe(e.getMessage());
        }

    }

    public void onAsmEdit(RowEditEvent event) {
        ComptypeAsm prt = (ComptypeAsm) event.getObject();

        try {
            Utility.showMessage(FacesMessage.SEVERITY_INFO, "Edit:", "Edit initiated");
        } catch (Exception e) {
            selectedParts.remove(prt);
            Utility.showMessage(FacesMessage.SEVERITY_ERROR, "Error:", e.getMessage());
        }
    }

    public void onAsmCancel(RowEditEvent event) {
        Utility.showMessage(FacesMessage.SEVERITY_INFO, "Row Cancel", "");
    }

    public void onAsmSave(RowEditEvent event) {
        ComptypeAsm prt = (ComptypeAsm) event.getObject();

        try {
            comptypeEJB.saveComptypeAsm(selectedObject, prt);
            Utility.showMessage(FacesMessage.SEVERITY_INFO, "Assembly item saved", "");

        } catch (Exception e) {
            selectedParts.remove(prt);
            Utility.showMessage(FacesMessage.SEVERITY_ERROR, "Error: Assembly item not saved", e.getMessage());
        }
    }

    // -------------------- Getters and Setters ---------------------------------------
    public List<ComponentType> getSortedObjects() {
        return sortedObjects;
    }

    public void setSortedObjects(List<ComponentType> sortedObjects) {
        this.sortedObjects = sortedObjects;
    }

    public List<ComponentType> getFilteredObjects() {
        return filteredObjects;
    }

    public void setFilteredObjects(List<ComponentType> filteredObjects) {
        this.filteredObjects = filteredObjects;
    }

    public ComponentType getSelectedObject() {
        return selectedObject;
    }

    public void setSelectedObject(ComponentType selectedObject) {
        this.selectedObject = selectedObject;
    }

    public ComponentType getInputObject() {
        return inputObject;
    }

    public void setInputObject(ComponentType inputObject) {
        this.inputObject = inputObject;
    }

    public List<ComponentType> getObjects() {
        return objects;
    }

    public boolean isInTrans() {
        return inTrans;
    }

    public ComptypeProperty getSelectedCTP() {
        return selectedProperty;
    }

    public void setSelectedCTP(ComptypeProperty selectedCTP) {
        this.selectedProperty = selectedCTP;
    }

    public List<ComptypeArtifact> getSelectedArtifacts() {
        return selectedArtifacts;
    }

    public List<ComptypeAsm> getSelectedParts() {
        return selectedParts;
    }

    public ComptypeArtifact getInputArtifact() {
        return inputArtifact;
    }

    public void setInputArtifact(ComptypeArtifact inputArtifact) {
        this.inputArtifact = inputArtifact;
    }

    public String getUploadedFileName() {
        return uploadedFileName;
    }

    public char getArtifactOperation() {
        return artifactOperation;
    }

    public boolean isInternalArtifact() {
        return internalArtifact;
    }

    public void setInternalArtifact(boolean internalArtifact) {
        this.internalArtifact = internalArtifact;
    }

    public ComptypeArtifact getSelectedArtifact() {
        return selectedArtifact;
    }

    public void setSelectedArtifact(ComptypeArtifact selectedArtifact) {
        this.selectedArtifact = selectedArtifact;
    }

    public List<ComptypeProperty> getSelectedProperties() {
        return selectedProperties;
    }

    public char getPropertyOperation() {
        return propertyOperation;
    }

    public ComptypeProperty getSelectedProperty() {
        return selectedProperty;
    }

    public void setSelectedProperty(ComptypeProperty selectedProperty) {
        this.selectedProperty = selectedProperty;
    }

    public ComptypeProperty getInputProperty() {
        return inputProperty;
    }

    public void setInputProperty(ComptypeProperty inputProperty) {
        this.inputProperty = inputProperty;
    }

    public boolean isInRepository() {
        return inRepository;
    }

    public void setInRepository(boolean inRepository) {
        this.inRepository = inRepository;
    }

}
