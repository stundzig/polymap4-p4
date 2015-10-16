package org.polymap.p4.data.imports.refine;

import java.util.Collection;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.google.common.collect.Lists;
import com.google.refine.model.Row;

public class RefineContentProvider
        implements IStructuredContentProvider {

    /**
     * 
     */
    private static final long            serialVersionUID = 1L;

    private static RefineContentProvider instance;


    private RefineContentProvider() {
    }


    public static RefineContentProvider getInstance() {
        synchronized (RefineContentProvider.class) {
            if (instance == null) {
                instance = new RefineContentProvider();
            }
            return instance;
        }
    }


    @Override
    public void dispose() {
        // TODO Auto-generated method stub

    }


    /**
     * Returns the elements in the input, which must be either an array or a
     * <code>Collection</code>.
     */
    public Object[] getElements( Object inputElement ) {
        List<Row> rows = (List<Row>)inputElement;
        List<RefineRow> elements = Lists.newArrayListWithCapacity( rows.size() );
        int index = 1;
        for (Row row : rows) {
            elements.add( new RefineRow( index, row ) );
            index++;
        }
        return elements.toArray();
    }


    @Override
    public void inputChanged( Viewer viewer, Object oldInput, Object newInput ) {
        // TODO Auto-generated method stub

    }

}
