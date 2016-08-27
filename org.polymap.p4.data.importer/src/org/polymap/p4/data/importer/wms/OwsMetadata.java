/* 
 * polymap.org
 * Copyright (C) 2016, the @authors. All rights reserved.
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
package org.polymap.p4.data.importer.wms;

import java.util.Arrays;
import java.util.List;

import org.geotools.data.ows.Layer;
import org.geotools.data.ows.Service;
import org.opengis.metadata.citation.Address;
import org.opengis.metadata.citation.Contact;
import org.opengis.metadata.citation.ResponsibleParty;
import org.opengis.metadata.citation.Telephone;

import org.apache.commons.lang3.StringUtils;

import org.polymap.core.runtime.i18n.IMessages;

import org.polymap.p4.data.importer.Messages;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public class OwsMetadata {

    private static final IMessages i18n = Messages.forPrefix( "OwsMetadata" );

    private StringBuilder       buf = new StringBuilder( 1024 );
    

    @Override
    public String toString() {
        return buf.toString();
    }


    public OwsMetadata markdown( Service service ) {
        markdownH3( service.getTitle() + " (" + service.getName() + ")" );
        markdown( service.get_abstract() );
        
        if (service.getOnlineResource() != null) {
            //markdownH3( i18n.get( "onlineResource" ) );
            markdown( "*" + service.getOnlineResource().toString() + "*" );
        }

        markdown( service.getContactInformation() );
        return this;
    }
    
    
    public OwsMetadata markdown( ResponsibleParty party ) {
        if (party != null) {
            //markdownH3( i18n.get( "contactInfo" ) );
            markdownH3( StringUtils.defaultIfBlank( party.getOrganisationName().toString(), party.getIndividualName() ) );
            markdownListItem( /*"individualName",*/ party.getIndividualName() );
            //markdownListItem( "organisationName", party.getOrganisationName() );
            markdownListItem( /*"positionName",*/ party.getPositionName() );
            buf.append( "\n" );
            
            markdown( party.getContactInfo() );
        }
        return this;
    }

    
    public OwsMetadata markdown( Contact contact ) {
        if (contact != null) {
            markdown( contact.getAddress() );
            markdown( contact.getPhone() );

            markdownListItem( "hoursOfService", contact.getHoursOfService() );
            buf.append( "\n" );
        }
        return this;
    }


    public OwsMetadata markdown( Telephone phone ) {
        if (phone != null) {
            markdownListItem( "phoneVoices", join( ", ", phone.getVoices() ) );
            markdownListItem( "phoneFacsimiles", join( ", ", phone.getFacsimiles() ) );
        }
        return this;
    }


    public OwsMetadata markdown( Address address ) {
        if (address != null) {
            int start = buf.length();
            markdownListItem( join( ", ", address.getDeliveryPoints() ) );
            markdownListItem( join( " ", address.getPostalCode(), address.getCity() ) );
            markdownListItem( join( ", ", address.getAdministrativeArea(), address.getCountry() ) );
            markdownListItem( join( ", ", address.getElectronicMailAddresses() ) );
            
//            markdownListItem( "country", address.getCountry() );
//            markdownListItem( "administrativeArea", address.getAdministrativeArea() );
//            markdownListItem( "city", address.getCity() );
//            markdownListItem( "postalCode", address.getPostalCode() );
//            markdownListItem( "deliveryPoints", Joiner.on( ", " ).join( address.getDeliveryPoints() ) );
//            markdownListItem( "emails", Joiner.on( ", " ).join( address.getElectronicMailAddresses() ) );
            
            if (buf.length() > start) {
                buf.insert( start, new OwsMetadata().markdownH3( i18n.get( "address" ) ).toString() );
            }
        }
        return this;
    }

    
    public OwsMetadata markdown( List<Layer> layers ) {
        markdownH3( i18n.get( "layers" ) );
        for (Layer layer : layers) {
            markdownListItem( layer.getTitle() );
        }
        return this;
    }
    
    
    public OwsMetadata markdownH3( String name ) {
        buf.append( "#### " ).append( name ).append( "\n\n" );
        return this;
    }
    
    
    public OwsMetadata markdownListItem( String name, CharSequence value ) {
        if (!StringUtils.isBlank( value )) {
            buf.append( "  * " ).append( i18n.get( name ) ).append( ": " ).append( value ).append( "\n" );
        }
        return this;
    }
    
    public OwsMetadata markdownListItem( CharSequence value ) {
        if (!StringUtils.isBlank( value )) {
            buf.append( "  * " ).append( value ).append( "\n" );
        }
        return this;
    }
    
    public OwsMetadata markdown( CharSequence paragraph ) {
        if (!StringUtils.isBlank( paragraph )) {
            buf.append( paragraph ).append( "\n\n" );
        }
        return this;
    }
    
    public String join( String delim, Iterable<? extends CharSequence> parts ) {
        StringBuffer result = new StringBuffer( 256 );
        int c = 0;
        for (CharSequence part : parts) {
            if (!StringUtils.isBlank( part )) {
                result.append( c++ > 0 ? delim : "" ).append( part );
            }
        }
        return result.toString();
    }
    
    public String join( String delim, CharSequence... parts ) {
        return join( delim, Arrays.asList( parts ) );
    }
    
}
