/*
 * polymap.org 
 * Copyright (C) @year@ individual contributors as indicated by the @authors tag. 
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
package org.polymap.p4.data.imports.refine.csv;

import static org.polymap.rhei.batik.app.SvgImageRegistryHelper.NORMAL24;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.osgi.framework.ServiceReference;
import org.polymap.core.data.refine.RefineService;
import org.polymap.core.data.refine.impl.CSVFormatAndOptions;
import org.polymap.core.data.refine.impl.ImportResponse;
import org.polymap.p4.P4Plugin;
import org.polymap.p4.data.imports.ContextIn;
import org.polymap.p4.data.imports.ContextOut;
import org.polymap.p4.data.imports.Importer;
import org.polymap.p4.data.imports.ImporterPrompt;
import org.polymap.p4.data.imports.ImporterPrompt.PromptUIBuilder;
import org.polymap.p4.data.imports.ImporterSite;
import org.polymap.p4.data.imports.refine.AbstractRefineFileImporter;
import org.polymap.p4.data.imports.refine.RefineContentProvider;
import org.polymap.p4.data.imports.refine.RefineRow;
import org.polymap.rhei.batik.toolkit.md.MdToolkit;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.refine.importing.ImportingJob;
import com.google.refine.model.Cell;
import com.google.refine.model.Column;
import com.google.refine.model.ColumnModel;

/**
 * @author <a href="http://stundzig.it">Steffen Stundzig</a>
 */
public class CSVFileImporter extends AbstractRefineFileImporter<CSVFormatAndOptions> {

    private static Log log = LogFactory.getLog(CSVFileImporter.class);

    @Override
    public void init(@SuppressWarnings("hiding") ImporterSite site, IProgressMonitor monitor) throws Exception {
        this.site = site;

        site.icon.set(P4Plugin.images().svgImage("csv.svg", NORMAL24));
        site.summary.set("CSV / TSV / separator based file: " + file.getName());

        super.init(site, monitor);
    }

    @Override
    public void createPrompts(IProgressMonitor monitor) throws Exception {
        // charset prompt
        site.newPrompt("encoding").summary.put("Zeichensatz der Daten").description
                .put("Die Daten können bspw. deutsche Umlaute enthalten, die nach dem Hochladen falsch dargestellt werden. "
                        + "Mit dem Ändern des Zeichensatzes kann dies korrigiert werden.").extendedUI
                                .put(new PromptUIBuilder() {

                                    @Override
                                    public void submit(ImporterPrompt prompt) {
                                        // TODO Auto-generated method stub

                                    }

                                    @Override
                                    public void createContents(ImporterPrompt prompt, Composite parent) {
                                        // select box
                                        Combo combo = new Combo(parent, SWT.SINGLE);
                                        List<String> encodings = Lists.newArrayList(Charsets.ISO_8859_1.name(),
                                                Charsets.US_ASCII.name(), Charsets.UTF_8.name(), Charsets.UTF_16.name(),
                                                Charsets.UTF_16BE.name(), Charsets.UTF_16LE.name());

                                        // java.nio.charset.Charset.forName( )
                                        combo.setItems(encodings.toArray(new String[encodings.size()]));
                                        // combo.add

                                        combo.addSelectionListener(new SelectionAdapter() {

                                            @Override
                                            public void widgetSelected(SelectionEvent e) {
                                                Combo c = (Combo) e.getSource();
                                                String selected = encodings.get(c.getSelectionIndex());
                                                formatAndOptions().setEncoding(selected);
                                                updateOptions();
                                                prompt.ok.set(true);
                                            }
                                        });
                                        int index = encodings.indexOf(formatAndOptions().encoding());
                                        if (index != -1) {
                                            combo.select(index);
                                        }
                                    }
                                });
        site.newPrompt("headline").summary.put("Kopfzeile").description
                .put("Welche Zeile enhält die Spaltenüberschriften?").extendedUI.put(new PromptUIBuilder() {
                    @Override
                    public void createContents(ImporterPrompt prompt, Composite parent) {
                        // TODO use a rhei numberfield here
                        Text text = new Text(parent, SWT.RIGHT);
                        text.setText(formatAndOptions().headerLines());
                        text.addModifyListener(event -> {
                            Text t = (Text) event.getSource();
                            // can throw an exception
                            int index = Integer.parseInt(t.getText());
                            formatAndOptions().setHeaderLines(index);
                            updateOptions();
                            prompt.ok.set(true);
                        });
                    }

                    @Override
                    public void submit(ImporterPrompt prompt) {
                        // TODO Auto-generated method stub

                    }
                });
    }

    @Override
    protected CSVFormatAndOptions defaultOptions() {
        return CSVFormatAndOptions.createDefault();
    }
}
