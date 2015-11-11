/*
 * polymap.org 
 * Copyright (C) 2015 individual contributors as indicated by the @authors tag. 
 * All rights reserved.
 * 
 * This is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package org.polymap.p4.imports;

import static org.polymap.p4.imports.formats.IFileFormat.getFileExtension;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IStatus;
import org.polymap.core.catalog.MetadataQuery;
import org.polymap.core.runtime.event.EventManager;
import org.polymap.p4.P4Plugin;
import org.polymap.p4.catalog.LocalCatalog;
import org.polymap.p4.imports.formats.ArchiveFormats;
import org.polymap.p4.imports.formats.FileDescription;
import org.polymap.p4.imports.formats.ShapeFileDescription;
import org.polymap.p4.imports.formats.ShapeFileFormats;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

/**
 * <p>
 * validations:
 * <ul>
 * <li>global
 * <ul>
 * <li><b>error:</b> duplicate among uploaded file groups</li>
 * </ul>
 * </li>
 * <li>per common name = group
 * <ul>
 * <li><b>error:</b> group with that name has been already imported as catalog entry</li>
 * <li><b>error:</b> group is empty (either archive is empty or cannot be read)</li>
 * <li><b>error:</b> invalid file type for an archive: .zip, .tar, .gz, .gzip</li>
 * <li><b>error:</b> missing required file types: [name].dbf, [name].shp, [name].shx</li>
 * <li><b>warning:</b> missing optional file types: [name].atx, [name].aih,
 * [name].cpg, [name].prj, [name].qix, [name].sbx, [name].shp.xml</li>
 * </ul>
 * </li>
 * <li>per file
 * <ul>
 * <li><b>error:</b> invalid file type as not in [name].dbf, [name].shp, [name].shx,
 * [name].atx, [name].aih, [name].cpg, [name].fbx, [name].fbn, [name].prj,
 * [name].qix, [name].sbx, [name].shp.xml</li>
 * <li>(<b>error:</b> file is corrupt / cannot be parsed, only be detectable when
 * importing)</li>
 * </ul>
 * </li>
 * </ul>
 * </p>
 * <p>
 * re-validation:
 * <ul>
 * <li>upload
 * <ul>
 * <li>group(s) resp. archive file
 * <ul>
 * <li>retrigger group validation for each new group</li>
 * <li>retrigger duplicate validation</li>
 * </ul>
 * </li>
 * <li>single file (of existing group)
 * <ul>
 * <li>duplicate file will be replaced?
 * <li>
 * <li>retrigger file type validation</li>
 * <li>retrigger all required file type validation for containing group</li>
 * <li>retrigger all optional file type validation for containing group</li>
 * </ul>
 * </li></li>
 * <li>delete
 * <ul>
 * <li>deletion of group resp. deletion of last file of group
 * <ul>
 * <li>retrigger duplicate validation</li>
 * <li>vanish validation issue for existing catalog entry</li>
 * <li>vanish all validation issues for removed files</li>
 * </ul>
 * </li>
 * <li>deletion of file
 * <ul>
 * <li>vanish all validation issues for removed file</li>
 * </ul>
 * </li>
 * </ul>
 * </li>
 * </ul>
 * </p>
 * <p>
 * 2nd line update:
 * <ul>
 * <li>upload</li>
 * <li>delete</li>
 * <li>expand tree node</li>
 * <li>collapse tree node</li>
 * </ul>
 * </p>
 * 
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class ShapeFileValidator {

    private static List<String> VALID_ARCHIV_FILE_EXTS         = Arrays.asList( ArchiveFormats.values() ).stream()
                                                                       .map( f -> f.getFileExtension() )
                                                                       .collect( Collectors.toList() );

    private static List<String> REQUIRED_VALID_SHAPE_FILE_EXTS = Lists.newArrayList(
                                                                       ShapeFileFormats.DBF.getFileExtension(),
                                                                       ShapeFileFormats.SHP.getFileExtension(),
                                                                       ShapeFileFormats.SHX.getFileExtension() );

    private static List<String> OPTIONAL_VALID_SHAPE_FILE_EXTS = Arrays.asList( ShapeFileFormats.values() )
                                                                       .stream()
                                                                       .map( f -> f.getFileExtension() )
                                                                       .filter(
                                                                               e -> !REQUIRED_VALID_SHAPE_FILE_EXTS
                                                                                       .contains( e ) )
                                                                       .collect( Collectors.toList() );

    private LocalCatalog        localCatalog                   = null;


    /* *** validate by traversing everything *** */

    public boolean validateAll( List<FileDescription> files ) {
        boolean valid = true;
        valid &= validateNonExistingCatalogEntries( files );
        if (valid) {
            valid &= validateDuplicates( files );
            if (valid) {
                for (FileDescription fd : files) {
                    valid &= validate( fd );
                }
            }
        }
        return valid;
    }


    /**
     * @param keySet
     * @return
     */
    private boolean validateNonExistingCatalogEntries( List<FileDescription> files ) {
        Set<String> names = files.stream().map( file -> file.groupName.get() ).collect( Collectors.toSet() );
        Function<String,Boolean> predicate = ( String title ) -> names.contains( title );
        Function<String,Optional<FileDescription>> getFileDescriptionByGroupName = ( String groupName ) -> files
                .stream().filter( file -> groupName.equals( file.groupName.get() ) ).findFirst();
        return validateNonExistingCatalogEntry( predicate, getFileDescriptionByGroupName );
    }


    /* *** validate set of groups of files *** */

    /**
     * @param files
     * @return
     */
    private boolean validateDuplicates( List<FileDescription> files ) {
        Set<String> set = new HashSet<String>();
        List<FileDescription> duplicates = new ArrayList<FileDescription>();
        files.stream().forEach( root -> root.getContainedFiles().stream().forEach( cf -> {
            if (!set.add( cf.name.get() )) {
                duplicates.add( cf );
            }
        } ) );
        for (FileDescription duplicate : duplicates) {
            reportError( duplicate, duplicate.name.get() + " is contained more then once in the files to be imported." );
        }
        return duplicates.isEmpty();
    }


    /* *** validate group of files *** */

    private boolean validateNonExistingCatalogEntry( FileDescription root ) {
        Function<String,Boolean> predicate = ( String title ) -> root.groupName.isPresent()
                && root.groupName.get().equals( title );
        Function<String,Optional<FileDescription>> getFileDescriptionByGroupName = ( String groupName ) -> Optional
                .of( root );
        return validateNonExistingCatalogEntry( predicate, getFileDescriptionByGroupName );
    }


    public boolean validate( FileDescription fd ) {
        if (!fd.parentFile.isPresent()) {
            return validateRoot( fd );
        }
        else {
            return validateSingle( fd );
        }
    }


    private boolean validateRoot( FileDescription root ) {
        boolean valid = hasValidRootFileExtension( root );
        valid &= validateNonExistingCatalogEntry( root );
        if (valid) {
            for (FileDescription fd : root.getContainedFiles()) {
                valid &= validate( fd );
            }
            if (valid) {
                if (root instanceof ShapeFileDescription) {
                    ShapeFileDescription shapeRoot = (ShapeFileDescription)root;
                    valid &= containsShpFile( shapeRoot );
                    if (valid) {
                        valid &= containsAllRequiredFiles( shapeRoot );
                    }
                    if (valid) {
                        valid &= containsAllOptionalFiles( shapeRoot );
                    }
                }
            }
        }
        return valid;
    }


    private boolean validateSingle( FileDescription fd ) {
        boolean validRequired = hasValidShapeFileExtension( fd );
        boolean validOptional = hasValidOptionalShapeFileExtension( fd );
        boolean valid = validRequired || validOptional;
        if (!valid) {
            handleInvalidShapeFileExtension( fd, validOptional );
        }
        return valid;
    }


    /**
     * @param files
     * @return
     */
    private boolean containsAllRequiredFiles( ShapeFileDescription root ) {
        boolean valid = true;
        Set<String> fileExtensions = root.getContainedFiles().stream()
                .map( f -> StringUtils.lowerCase( getFileExtension( f.file.get().getName() ) ) )
                .collect( Collectors.toSet() );
        List<String> missing = new ArrayList<String>();
        for (String ext : REQUIRED_VALID_SHAPE_FILE_EXTS) {
            if (!fileExtensions.contains( ext )) {
                valid = false;
                missing.add( root + "." + ext );
            }
        }
        if (!valid) {
            reportError( root, Joiner.on( ", " ).join( missing ) + " must be provided." );
        }
        return valid;
    }


    /**
     * @param files
     * @return
     */
    private boolean containsAllOptionalFiles( ShapeFileDescription root ) {
        boolean valid = true;
        Set<String> fileExtensions = root.getContainedFiles().stream()
                .map( f -> StringUtils.lowerCase( getFileExtension( f.file.get().getName() ) ) )
                .collect( Collectors.toSet() );
        List<String> missing = new ArrayList<String>();
        for (String ext : OPTIONAL_VALID_SHAPE_FILE_EXTS) {
            if (!fileExtensions.contains( ext )) {
                missing.add( root.groupName.get() + "." + ext );
            }
        }
        if (missing.size() > 0) {
            reportWarning( root, Joiner.on( ", " ).join( missing ) + " might be required as well." );
        }
        return valid;
    }


    /**
     * @param files
     * @return
     */
    private boolean containsShpFile( ShapeFileDescription root ) {
        boolean valid = root
                .getContainedFiles()
                .stream()
                .anyMatch(
                        f -> ShapeFileFormats.SHP.getFileExtension().equalsIgnoreCase(
                                getFileExtension( f.file.get().getName() ) ) );
        if (!valid) {
            reportError( root, root + ".shp isn't provided." );
        }
        return valid;
    }


    /* *** validate single file *** */

    private void handleInvalidShapeFileExtension( FileDescription fd, boolean validOptional ) {
        String fileExtension = getFileExtension( fd.file.get().getName() );
        if (!validOptional) {
            reportError( fd, fileExtension + " is not a valid shape file extension" );
        }
        else {
            reportError( fd, fileExtension + " is not a valid shape file extension" );
        }
    }


    private boolean hasValidRootFileExtension( FileDescription fd ) {
        boolean validRequired = hasValidShapeFileExtension( fd );
        boolean validOptional = hasValidOptionalShapeFileExtension( fd );
        boolean validArchive = hasValidArchiveFileExtension( fd );
        boolean valid = validRequired || validOptional || validArchive;
        if (!valid) {
            if (!validArchive) {
                String fileExtension = internalGetFileExtension( fd );
                reportError( fd, fileExtension + " is not a valid archive file extension" );
            }
            else {
                handleInvalidShapeFileExtension( fd, validOptional );
            }
        }
        return valid;
    }


    private boolean hasValidArchiveFileExtension( FileDescription fd ) {
        return hasValidFileExtension( fd, VALID_ARCHIV_FILE_EXTS );
    }


    private boolean hasValidShapeFileExtension( FileDescription fd ) {
        return hasValidFileExtension( fd, REQUIRED_VALID_SHAPE_FILE_EXTS );
    }


    private boolean hasValidOptionalShapeFileExtension( FileDescription fd ) {
        return hasValidFileExtension( fd, OPTIONAL_VALID_SHAPE_FILE_EXTS );
    }


    private boolean hasValidFileExtension( FileDescription fd, List<String> validExtensions ) {
        if (!fd.name.isPresent() && !fd.file.isPresent()) {
            return !fd.parentFile.isPresent();
        }
        String fileExtension = internalGetFileExtension( fd );
        return validExtensions.stream().anyMatch( ext -> ext.equalsIgnoreCase( fileExtension ) );
    }


    private String internalGetFileExtension( FileDescription fd ) {
        String fileExtension = null;
        if (fd.name.isPresent()) {
            fileExtension = getFileExtension( fd.name.get() );
        }
        else if (fd.file.isPresent()) {
            File file = fd.file.get();
            fileExtension = getFileExtension( file.getName() );
        }
        return fileExtension;
    }


    /* *** utils *** */

    private boolean validateNonExistingCatalogEntry( Function<String,Boolean> predicate,
            Function<String,Optional<FileDescription>> getFileDescription ) {
        MetadataQuery entries = getLocalCatalog().query( "" );
        return entries.execute().stream().noneMatch( e -> {
            String title = e.getTitle().replace( ".shp", "" );
            boolean contains = predicate.apply( title );
            if (contains) {
                Optional<FileDescription> fileDescOpt = getFileDescription.apply( title );
                if (fileDescOpt.isPresent()) {
                    reportError( fileDescOpt.get(), e.getTitle() + " is already imported as catalog entry." );
                }
            }
            return contains;
        } );
    }


    void setLocalCatalog( LocalCatalog localCatalog ) {
        this.localCatalog = localCatalog;
    }


    LocalCatalog getLocalCatalog() {
        if (localCatalog == null) {
            localCatalog = P4Plugin.localCatalog();
        }
        return localCatalog;
    }


    /* *** report issues *** */

    public static void reportError( Object source, String message ) {
        reportIssue( source, IStatus.ERROR, message );
    }


    public static void reportWarning( Object source, String message ) {
        reportIssue( source, IStatus.WARNING, message );
    }


    private static void reportIssue( Object source, int severity, String message ) {
        publish( getEventManager(), new ValidationEvent( source, severity, message ) );
    }


    private static void publish( EventManager eventManager, ValidationEvent validationEvent ) {
        eventManager.publish( validationEvent );
    }

    private static EventManager eventManager = null;


    static void setEventManager( EventManager aEventManager ) {
        eventManager = aEventManager;
    }


    static EventManager getEventManager() {
        if (eventManager == null) {
            eventManager = EventManager.instance();
        }
        return eventManager;
    }
}
