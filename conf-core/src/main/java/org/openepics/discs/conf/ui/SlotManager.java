/*
 * Copyright (c) 2014 European Spallation Source
 * Copyright (c) 2014 Cosylab d.d.
 *
 * This file is part of Controls Configuration Database.
 *
 * Controls Configuration Database is free software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the License,
 * or any newer version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.openepics.discs.conf.ui;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.openepics.discs.conf.dl.SlotPairDataLoader;
import org.openepics.discs.conf.dl.SlotsAndSlotPairsDataLoader;
import org.openepics.discs.conf.dl.SlotsDataLoader;
import org.openepics.discs.conf.dl.common.AbstractDataLoader;
import org.openepics.discs.conf.dl.common.DataLoaderResult;
import org.openepics.discs.conf.dl.common.ExcelImportFileReader;
import org.openepics.discs.conf.ui.common.ExcelImportUIHandlers;
import org.primefaces.event.FileUploadEvent;

import com.google.common.io.ByteStreams;

/**
 *
 * @author vuppala
 * @author <a href="mailto:miha.vitorovic@cosylab.com">Miha Vitorovič</a>
 */
@Named
@ViewScoped
public class SlotManager implements Serializable, ExcelImportUIHandlers {
    private static final long serialVersionUID = 7271953102489952318L;

    @Inject private transient SlotsAndSlotPairsDataLoader slotsAndSlotPairsLoader;

    private byte[] importSlotData, importSlotRelationshipsData;
    private String firstFileName, secondFileName;
    private transient DataLoaderResult loaderResult;

    /**
     * Creates a new instance of SlotManager
     */
    public SlotManager() {
    }

    public String getFirstFileName() {
        return firstFileName;
    }
    public String getSecondFileName() {
        return secondFileName;
    }

    @Override
    public void doImport() {
        try (
            // try-with-resources works also with "null" streams by design.
            // reference: https://blogs.oracle.com/darcy/entry/project_coin_null_try_with
            InputStream slotInputStream = importSlotData != null ? new ByteArrayInputStream(importSlotData) : null;
            InputStream slotPairsInputStream = importSlotRelationshipsData != null
                                                    ? new ByteArrayInputStream(importSlotRelationshipsData) : null;
        ) {
            loaderResult = loadDataFromTwoFiles(slotInputStream, slotPairsInputStream, firstFileName, secondFileName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public DataLoaderResult getLoaderResult() {
        return loaderResult;
    }

    @Override
    public void prepareImportPopup() {
        loaderResult = null;
        importSlotData = null;
        firstFileName = null;
        importSlotRelationshipsData = null;
        secondFileName = null;
    }

    /** This method is called when user clicks the "Upload" button in the "excel import" UI. This action is called when
     * importing the Slot information excel worksheet. The data is stored on the server to be parsed if the user actually
     * decides to process the import data (he can still cancel the action instead).
     * @param event The PrimeFaces upload event.
     */
    public void handleFirstImportFileUpload(FileUploadEvent event) {
        try (InputStream inputStream = event.getFile().getInputstream()) {
            importSlotData = ByteStreams.toByteArray(inputStream);
            firstFileName = FilenameUtils.getName(event.getFile().getFileName());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /** This method is called when user clicks the "Upload" button in the "excel import" UI. This action is called when
     * importing the Slot relationships information excel worksheet. The data is stored on the server to be parsed if
     * the user actually decides to process the import data (he can still cancel the action instead).
     * @param event The PrimeFaces upload event.
     */
    public void handleSecondImportFileUpload(FileUploadEvent event) {
        try (InputStream inputStream = event.getFile().getInputstream()) {
            importSlotRelationshipsData = ByteStreams.toByteArray(inputStream);
            secondFileName = FilenameUtils.getName(event.getFile().getFileName());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Loads data from two import files to {@link List} and calls method on certain data loader
     * to save the data in the database. If the result of save is {@link DataLoaderResult#isError()}
     * then the transaction is rolled back. In any case, the notification is shown to the user.
     * The two files in this case are the Slots information Excel worksheet and Slot relationship Excel worksheet.
     *
     * @param slotsInputStream input file containing the Slots information Excel worksheet
     * @param slotPairsInputStream input file containing the Slot relationship Excel worksheet
     * @param slotsFileName the name of the Slots information Excel worksheet file
     * @param slotPairsFileName the name of the Slot relationship Excel worksheet file
     * @return a {@link DataLoaderResult} containing information about the operation completion status
     */
    private DataLoaderResult loadDataFromTwoFiles(InputStream slotsInputStream, InputStream slotPairsInputStream,
            String slotsFileName, String slotPairsFileName) {
        List<Pair<Integer, List<String>>> slotsFileInputRows = null;
        List<Pair<Integer, List<String>>> slotPairsFileInputRows = null;

        if (slotsInputStream != null) {
            slotsFileInputRows = ExcelImportFileReader.importExcelFile(slotsInputStream,
                    AbstractDataLoader.DEFAULT_EXCEL_TAMPLATE_DATA_START_ROW, SlotsDataLoader.DATA_WIDTH);
        } else {
            slotsFileInputRows = null;
        }

        if (slotPairsInputStream != null) {
            slotPairsFileInputRows = ExcelImportFileReader.importExcelFile(slotPairsInputStream,
                    AbstractDataLoader.DEFAULT_EXCEL_TAMPLATE_DATA_START_ROW, SlotPairDataLoader.DATA_WIDTH);
        } else {
            slotPairsFileInputRows = null;
        }
        return slotsAndSlotPairsLoader.loadDataToDatabase(slotsFileInputRows, slotPairsFileInputRows, slotsFileName,
                    slotPairsFileName);
    }

    @Override
    public ImportFileStatistics getImportedFileStatistics() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setDataLoader() {
        // TODO Auto-generated method stub
    }
}
