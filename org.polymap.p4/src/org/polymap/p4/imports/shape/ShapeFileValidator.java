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
package org.polymap.p4.imports.shape;

import java.io.File;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import org.eclipse.core.runtime.IStatus;

import org.polymap.core.catalog.MetadataQuery;
import org.polymap.core.runtime.event.EventManager;

import org.polymap.p4.P4Plugin;
import org.polymap.p4.imports.ValidationEvent;

import com.google.common.base.Joiner;

/**
 * <p>
 * validations:
 * <ul>
 *   <li>global
 *     <ul>
 *       <li><b>error:</b> duplicate among uploaded file groups</li>
 *     </ul>
 *   </li>    
 *   <li>per common name = group
 *     <ul>
 *       <li><b>error:</b> group with that name has been already imported as catalog entry</li>
 *       <li><b>error:</b> group is empty (either archive is empty or cannot be read)</li>
 *       <li><b>error:</b> invalid file type for an archive: .zip, .tar, .gz, .gzip</li>
 *       <li><b>error:</b> missing required file types: [name].dbf, [name].shp, [name].shx</li>
 *       <li><b>warning:</b> missing optional file types: [name].atx, [name].aih, [name].cpg, [name].prj, [name].qix, [name].sbx, [name].shp.xml</li>
 *     </ul>
 *   </li>
 *   <li>per file
 *     <ul>
 *       <li><b>error:</b> invalid file type as not in [name].dbf, [name].shp, [name].shx, [name].atx, [name].aih, [name].cpg, [name].prj, [name].qix, [name].sbx, [name].shp.xml</li>
 *       <li>(<b>error:</b> file is corrupt / cannot be parsed, only be detectable when importing)</li>
 *     </ul>
 *   </li>
 * </ul>
 * </p>
 * <p>
 * re-validation:
 * <ul>
 *   <li>upload
 *     <ul>
 *       <li>group(s) resp. archive file
 *         <ul>
 *           <li>retrigger group validation for each new group</li>
 *           <li>retrigger duplicate validation</li>
 *         </ul>
 *       </li>
 *       <li>single file (of existing group)
 *         <ul>
 *           <li>duplicate file will be replaced?<li>
 *           <li>retrigger file type validation</li>
 *           <li>retrigger all required file type validation for containing group</li>
 *           <li>retrigger all optional file type validation for containing group</li>
 *         </ul>
 *       </li>
 *   </li>
 *   <li>delete
 *     <ul>
 *       <li>deletion of group resp. deletion of last file of group
 *         <ul>
 *           <li>retrigger duplicate validation</li>
 *           <li>vanish validation issue for existing catalog entry</li>
 *           <li>vanish all validation issues for removed files</li>
 *         </ul>
 *       </li>
 *       <li>deletion of file
 *         <ul>
 *           <li>vanish all validation issues for removed file</li>
 *         </ul>
 *       </li>
 *     </ul>
 *   </li>
 * </ul>
 * </p>
 * <p>
 * 2nd line update:
 * <ul>
 *   <li>upload
 *   </li>
 *   <li>delete
 *   </li>
 *   <li>expand tree node
 *   </li>
 *   <li>collapse tree node
 *   </li>
 * </ul>
 * </p>
 * 
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class ShapeFileValidator {

    private static String[] VALID_ARCHIV_FILE_EXTS         = { "zip", "tar", "tar.gz", "gzip" };

    private static String[] REQUIRED_VALID_SHAPE_FILE_EXTS = { "dbf", "shp", "shx" };

    private static String[] OPTIONAL_VALID_SHAPE_FILE_EXTS = { "atx", "aih", "cpg", "prj", "qix", "sbx", "shp.xml" };


    /* *** validate by traversing everything *** */

    public boolean validateAll( Map<String,Map<String,List<File>>> files ) {
        boolean valid = true;
        Set<String> names = files.values().stream().flatMap( map -> map.keySet().stream() )
                .collect( Collectors.toSet() );
        valid &= validateNonExistingCatalogEntries( names );
        if (valid) {
            valid &= validateDuplicates( files );
            if (valid) {
                for (Map<String,List<File>> entry : files.values()) {
                    valid &= validate( entry );
                }
            }
        }
        return valid;
    }

    /**
     * @param keySet
     * @return
     */
    private boolean validateNonExistingCatalogEntries( Set<String> names ) {
        Function<String,Boolean> predicate = (String title) -> names.contains( title );
        return validateNonExistingCatalogEntry( predicate );
    }

    /* *** validate by element type *** */

    /**
     * @param files
     * @param element
     */
    public boolean validate( Map<String,Map<String,List<File>>> files, Object element ) {
        if (element instanceof String) {
            Map<String,List<File>> entry = files.get( element );
            if (entry != null) {
                return validate( entry );
            }
        }
        else if (element instanceof File) {
            return validate( (File)element );
        }
        return false;
    }

    
    /* *** validate set of groups of files *** */

    /**
     * @param files
     * @return
     */
    private boolean validateDuplicates( Map<String,Map<String,List<File>>> files ) {
        Set<String> set = new HashSet<String>();
        List<File> duplicates = new ArrayList<File>();
        files.values().stream().forEach( map -> map.values().stream().forEach( fs -> fs.stream().forEach( f -> {
            if (!set.add( f.getName() )) {
                duplicates.add( f );
            }
        } ) ) );
        for (File duplicate : duplicates) {
            reportError( duplicate, duplicate.getName() + " is contained more then once in the files to be imported.");
        }
        return duplicates.isEmpty();
    }


    /* *** validate group of files *** */

    /**
     * @param grouped
     */
    public boolean validate( Map<String,List<File>> grouped ) {
        boolean valid = true;
        for (Map.Entry<String,List<File>> entry : grouped.entrySet()) {
            valid &= validate( entry.getKey(), entry.getValue() );
        }
        return valid;
    }

    
    private boolean validateNonExistingCatalogEntry( String name ) {
        Function<String,Boolean> predicate = (String title) -> name.equals( title );
        return validateNonExistingCatalogEntry( predicate );
    }

    
    public boolean validate( File root, List<File> files ) {
        boolean valid = hasValidRootFileExtension( root );
        if (valid) {
            String rootFileName = root.getName().replace( FilenameUtils.getExtension( root.getName() ), "" );
            valid &= validateNonExistingCatalogEntry( rootFileName );
            if (valid) {
                valid &= containsShpFile( rootFileName, files );
                if (valid) {
                    valid &= containsAllOptionalFiles( rootFileName, files );
                }
                if (valid) {
                    valid &= containsAllRequiredFiles( rootFileName, files );
                }
            }
        }
        return valid;
    }


    public boolean validate( String root, List<File> files ) {
        boolean valid = true;
        valid &= validateNonExistingCatalogEntry( root );
        if (valid) {
            for (File file : files) {
                valid &= validate( file );
            }
            if (valid) {
                valid &= containsShpFile( root, files );
                if (valid) {
                    valid &= containsAllRequiredFiles( root, files );
                }
                if (valid) {
                    valid &= containsAllOptionalFiles( root, files );
                }
            }
        }
        return valid;
    }


    /**
     * @param files
     * @return
     */
    private boolean containsAllRequiredFiles( String root, List<File> files ) {
        boolean valid = true;
        Set<String> fileExtensions = files.stream()
                .map( f -> StringUtils.lowerCase( FilenameUtils.getExtension( f.getName() ) ) )
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
    private boolean containsAllOptionalFiles( String root, List<File> files ) {
        boolean valid = true;
        Set<String> fileExtensions = files.stream()
                .map( f -> StringUtils.lowerCase( FilenameUtils.getExtension( f.getName() ) ) )
                .collect( Collectors.toSet() );
        List<String> missing = new ArrayList<String>();
        for (String ext : OPTIONAL_VALID_SHAPE_FILE_EXTS) {
            if (!fileExtensions.contains( ext )) {
                missing.add( root + "." + ext );
            }
        }
        if (missing.size() > 0) {
            reportWarning( root, Joiner.on( ", " ).join( missing ) + " might be required as well.");
        }
        return valid;
    }


    /**
     * @param files
     * @return
     */
    private boolean containsShpFile( String root, List<File> files ) {
        boolean valid = files.stream().anyMatch(
                f -> "shp".equalsIgnoreCase( FilenameUtils.getExtension( f.getName() ) ) );
        if (!valid) {
            reportError( root, root + ".shp isn't provided." );
        }
        return valid;
    }
    
    
    /* *** validate single file *** */

    public boolean validate( File file ) {
        boolean validRequired = hasValidShapeFileExtension( file );
        boolean validOptional = hasValidOptionalShapeFileExtension( file );
        boolean valid = validRequired || validOptional;
        if (!valid) {
            handleValidShapeFileExtension( file, validOptional );
        }
        return valid;
    }


    private void handleValidShapeFileExtension( File file, boolean validOptional ) {
        String fileExtension = FilenameUtils.getExtension( file.getName() );
        if (!validOptional) {
            reportError( file, fileExtension + " is not a valid shape file extension" );
        }
        else {
            reportError( file, fileExtension + " is not a valid shape file extension" );
        }
    }


    private boolean hasValidRootFileExtension( File file ) {
        boolean validRequired = hasValidShapeFileExtension( file );
        boolean validOptional = hasValidOptionalShapeFileExtension( file );
        boolean validArchive = hasValidArchiveFileExtension( file );
        boolean valid = validRequired || validOptional || validArchive;
        if (!valid) {
            if (!validArchive) {
                String fileExtension = FilenameUtils.getExtension( file.getName() );
                reportError( file, fileExtension + " is not a valid archive file extension" );
            }
            else {
                handleValidShapeFileExtension( file, validOptional );
            }
        }
        return valid;
    }


    private boolean hasValidArchiveFileExtension( File file ) {
        return hasValidFileExtension( file, VALID_ARCHIV_FILE_EXTS );
    }


    private boolean hasValidShapeFileExtension( File file ) {
        return hasValidFileExtension( file, REQUIRED_VALID_SHAPE_FILE_EXTS );
    }


    private boolean hasValidOptionalShapeFileExtension( File file ) {
        return hasValidFileExtension( file, OPTIONAL_VALID_SHAPE_FILE_EXTS );
    }


    private boolean hasValidFileExtension( File file, String[] validExtensions ) {
        String fileExtension = FilenameUtils.getExtension( file.getName() );
        if ("xml".equalsIgnoreCase( fileExtension )) {
            int index = file.getName().indexOf( ".shp.xml" );
            if (index > 0) {
                fileExtension = "shp.xml";
            }
        }
        for (String ext : validExtensions) {
            if (ext.equalsIgnoreCase( fileExtension )) {
                return true;
            }
        }
        return false;
    }
    
    /* *** utils *** */
    
    private boolean validateNonExistingCatalogEntry( Function<String,Boolean> predicate ) {
        MetadataQuery entries = P4Plugin.instance().localCatalog.query( "" );
        return entries
                .execute()
                .stream()
                .noneMatch(
                        e -> {
                            String title = e.getTitle().replace( ".shp", "" );
                            boolean contains = predicate.apply( title );
                            if (contains) {
                                reportError( title, e.getTitle() + " is already imported as catalog entry." );
                            }
                            return contains;
                        } );
    }

    
    /* *** report issues *** */

    public static void reportError( Object source, String message ) {
        reportIssue(source, IStatus.ERROR, message);
    }    

    public static void reportWarning( Object source, String message ) {
        reportIssue(source, IStatus.WARNING, message);
    }    

    private static void reportIssue( Object source, int severity, String message ) {
        EventManager.instance().publish( new ValidationEvent( source, severity, message));
    }    
}
