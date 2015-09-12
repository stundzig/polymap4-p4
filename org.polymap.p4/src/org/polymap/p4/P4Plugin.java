/* 
 * polymap.org
 * Copyright (C) 2013-2015, Falko Bräutigam. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.p4;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.util.tracker.ServiceTracker;
import org.polymap.core.CorePlugin;
import org.polymap.core.data.pipeline.DataSourceDescription;
import org.polymap.core.data.pipeline.Pipeline;
import org.polymap.core.data.pipeline.PipelineProcessor;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.IMap;
import org.polymap.core.ui.ImageRegistryHelper;
import org.polymap.model2.runtime.UnitOfWork;
import org.polymap.p4.catalog.LocalCatalog;
import org.polymap.p4.catalog.LocalResolver;
import org.polymap.p4.data.P4PipelineIncubator;
import org.polymap.p4.project.NewLayerContribution;
import org.polymap.p4.project.ProjectRepository;
import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.ant.ImageConfiguration;
import org.polymap.rhei.batik.ant.ImageConfiguration.ReplaceConfiguration;
import org.polymap.rhei.batik.ant.Scale;
import org.polymap.rhei.batik.ant.Svg2Png;
import org.polymap.rhei.batik.ant.Svg2Png.COLOR_TYPE;
import org.polymap.rhei.batik.contribution.ContributionManager;
import org.polymap.service.geoserver.GeoServerServlet;

/**
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class P4Plugin
        extends AbstractUIPlugin {

    private static Log         log                      = LogFactory.getLog( P4Plugin.class );

    public static final String ID                       = "org.polymap.p4";                   //$NON-NLS-1$

    /** The globale {@link Context} scope for the {@link P4Plugin}. */
    public static final String Scope                    = "org.polymap.p4";                   //$NON-NLS-1$

    private static P4Plugin    instance;

    private static String[]    IMAGE_CONTAINING_PLUGINS = new String[] { "org.polymap.p4", "org.polymap.rhei.batik",
            "org.polymap.core.mapeditor", "org.polymap.core.project", "org.polymap.core.catalog",
            "org.polymap.core.data", "org.polymap.core" };


    public static P4Plugin instance() {
        return instance;
    }

    // instance *******************************************

    private ImageRegistryHelper   images      = new ImageRegistryHelper( this );

    public LocalCatalog           localCatalog;

    public LocalResolver          localResolver;

    private ServiceTracker        httpServiceTracker;

    private Optional<HttpService> httpService = Optional.empty();


    public void start( BundleContext context ) throws Exception {
        super.start( context );
        instance = this;
        log.info( "Bundle state: " + getStateLocation() );
        log.info( "Bundle data: " + CorePlugin.getDataLocation( instance() ) );

        localCatalog = new LocalCatalog();
        localResolver = new LocalResolver( localCatalog );

        // register HTTP resource
        httpServiceTracker = new ServiceTracker( context, HttpService.class.getName(), null ) {

            public Object addingService( ServiceReference reference ) {
                httpService = Optional.ofNullable( (HttpService)super.addingService( reference ) );

                httpService.ifPresent( service -> {
                    // fake/test GeoServer
                        UnitOfWork uow = ProjectRepository.instance.get().newUnitOfWork();
                        IMap map = uow.entity( IMap.class, "root" );
                        try {
                            service.registerServlet( "/wms", new GeoServerServlet() {

                                @Override
                                public IMap getMap() {
                                    return map;
                                }


                                @Override
                                protected Pipeline createPipeline( ILayer layer,
                                        Class<? extends PipelineProcessor> usecase ) throws Exception {
                                    // resolve service
                                    NullProgressMonitor monitor = new NullProgressMonitor();
                                    DataSourceDescription dsd = LocalResolver
                                            .instance()
                                            .connectLayer( layer, monitor )
                                            .orElseThrow(
                                                    ( ) -> new RuntimeException( "No data source for layer: " + layer ) );

                                    // create pipeline for it
                                    return P4PipelineIncubator.forLayer( layer ).newPipeline( usecase, dsd, null );
                                }
                            }, null, null );
                        }
                        catch (Exception e) {
                            throw new RuntimeException( e );
                        }
                    } );

                return httpService.get();
            }
        };
        httpServiceTracker.open();

        ContributionManager.addStaticSupplier( ( ) -> new NewLayerContribution() );
        // ContributionManager.addStaticSupplier( () -> new TestProjectContribution()
        // );
    }


    public void stop( BundleContext context ) throws Exception {
        httpServiceTracker.close();
        localCatalog.close();

        instance = null;
        super.stop( context );
    }


    public HttpService httpService() {
        return httpService.orElseThrow( ( ) -> new IllegalStateException( "No HTTP service!" ) );
    }


    public Image imageForDescriptor( ImageDescriptor descriptor, String key ) {
        return images.image( descriptor, key );
    }


    public Image imageForName( String path ) {
        return images.image( imageDescriptor( path ), path );
    }


    public ImageDescriptor imageDescriptor( String path ) {
        ImageRegistry imageRegistry = images.getImageRegistry();
        ImageDescriptor image = imageRegistry.getDescriptor( path );
        if (image == null) {
            for (String pluginId : IMAGE_CONTAINING_PLUGINS) {
                image = AbstractUIPlugin.imageDescriptorFromPlugin( pluginId, path );
                if (image != null) {
                    imageRegistry.put( path, image );
                    break;
                }
            }
            if (image == null) {
                int index = path.lastIndexOf( "/" );
                if (index >= 0) {
                    String imageFile = path.substring( index + 1 );
                    String completeFolderPath = path.substring( 0, index );
                    index = completeFolderPath.lastIndexOf( "/" );
                    if (index >= 0) {
                        String size = completeFolderPath.substring( index + 1 );
                        String folderPath = completeFolderPath.substring( 0, index );
                        index = folderPath.lastIndexOf( "/" );
                        if (index >= 0) {
                            String colorScheme = folderPath.substring( index + 1 );
                            folderPath = folderPath.substring( 0, index );
                            String svgPath = folderPath + "/" + imageFile.replace( ".png", ".svg" );
                            svgPath = svgPath.replace( "/png/", "/svg/" );
                            try (InputStream svgInput = getClass().getClassLoader().getResourceAsStream( svgPath )) {
                                ImageConfiguration imageConfig = new ImageConfiguration();
                                imageConfig.setName( colorScheme );
                                if ("gray".equals( colorScheme )) {
                                    imageConfig.setRGB( new RGB( 95, 95, 95 ) );
                                    ReplaceConfiguration replaceConfig = new ReplaceConfiguration( new RGB( 0, 0, 0 ),
                                            new RGB( 95, 95, 95 ) );
                                    imageConfig.getReplaceConfigurations().add( replaceConfig );
                                    imageConfig.setColorType( COLOR_TYPE.ARGB );
                                }
                                Svg2Png svg2Png = new Svg2Png();
                                Scale scale = Scale.getAsScale( Integer.valueOf( size ) );
                                Path temp = Files.createTempDirectory( ID );
                                Path absolutePNGPath = temp.resolve( folderPath );
                                svg2Png.transcode( absolutePNGPath.toString(), imageFile.replace( ".png", ".svg" ),
                                        svgInput, Collections.singletonList( scale ),
                                        Collections.singletonList( imageConfig ) );
                                Path absolutePNGFilePath = temp.resolve( completeFolderPath ).resolve( imageFile );
                                image = ImageDescriptor.createFromURL( absolutePNGFilePath.toFile()
                                        .toURI().toURL() );
                                if (image != null) {
                                    imageRegistry.put( path, image );
                                }
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
        return image;
    }

}
