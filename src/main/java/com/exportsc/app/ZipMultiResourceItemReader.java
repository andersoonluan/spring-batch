/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.exportsc.app;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;

/**
 *
 * @author Anderson_Rodrigues2
 */
public class ZipMultiResourceItemReader<T> extends MultiResourceItemReader<T> {

    private static final Logger LOG = LoggerFactory.getLogger(ZipMultiResourceItemReader.class);
    private Resource[] archives;
    private ZipFile[] zipFiles;

    /**
     * Tries to extract all files in the archives and adds them as resources to
     * the normal MultiResourceItemReader. Overwrites the Comparator from
     * the super class to get it working with itemstreams.
     *
     * @param executionContext
     * @throws ItemStreamException 
     */
    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        // really used with archives?
        if (archives != null) {
            // overwrite the comparator to use description
            // instead of filename, the itemStream can only
            // have that description
            this.setComparator(new Comparator<Resource>() {

                /** Compares resource descriptions. */
                @Override
                public int compare(Resource r1, Resource r2) {
                    return r1.getDescription().compareTo(r2.getDescription());
                }
            });
            // get the inputStreams from all files inside the archives
            zipFiles = new ZipFile[archives.length];
            List<Resource> extractedResources = new ArrayList<Resource>();
            try {
                for (int i = 0; i < archives.length; i++) {
                    // find files inside the current zip resource
                    zipFiles[i] = new ZipFile(archives[i].getFile());
                    extractFiles(zipFiles[i], extractedResources);
                }
            } catch (Exception ex) {
                throw new ItemStreamException(ex);
            }
            // propagate extracted resources
            this.setResources(extractedResources.toArray(new Resource[extractedResources.size()]));
        }
        super.open(executionContext);
    }

    /**
     * Calls super.close() and tries to close all used zip files.
     *
     * @throws ItemStreamException 
     */
    @Override
    public void close() throws ItemStreamException {
        super.close();
        // try to close all used zipfiles
        if (zipFiles != null) {
            for (int i = 0; i < zipFiles.length; i++) {
                try {
                    zipFiles[i].close();
                } catch (IOException ex) {
                    throw new ItemStreamException(ex);
                }
            }
        }
    }

    /**
     * Extract only files from the zip archive.
     *
     * @param currentZipFile
     * @param extractedResources
     * @throws IOException 
     */
    private static void extractFiles(final ZipFile currentZipFile, final List<Resource> extractedResources) throws IOException {
        Enumeration<? extends ZipEntry> zipEntryEnum = currentZipFile.entries();
        while (zipEntryEnum.hasMoreElements()) {
            ZipEntry zipEntry = zipEntryEnum.nextElement();
            LOG.info("extracting:" + zipEntry.getName());
            // traverse directories
            if (!zipEntry.isDirectory()) {
                // add inputStream
                extractedResources.add(
                        new InputStreamResource(
                        currentZipFile.getInputStream(zipEntry),
                        zipEntry.getName()));
                LOG.info("using extracted file:" + zipEntry.getName());
            }
        }
    }

    /**
     * Set archive files with normal Spring resources pattern, if not set, the
     * class will fallback to normal MultiResourceItemReader behaviour.
     *
     * @param archives 
     */
    public void setArchives(Resource[] archives) {
        this.archives = archives;
    }
}
