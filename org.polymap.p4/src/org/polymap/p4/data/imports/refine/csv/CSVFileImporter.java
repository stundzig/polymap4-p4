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

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.polymap.core.data.refine.impl.CSVFormatAndOptions;
import org.polymap.p4.P4Plugin;
import org.polymap.p4.data.imports.ImporterPrompt;
import org.polymap.p4.data.imports.ImporterPrompt.PromptUIBuilder;
import org.polymap.p4.data.imports.ImporterSite;
import org.polymap.p4.data.imports.refine.AbstractRefineFileImporter;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;

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
        site.description.set("");

        super.init(site, monitor);
    }

    @Override
    public void createPrompts(IProgressMonitor monitor) throws Exception {
        // charset prompt
        site.newPrompt("encoding").summary.put("Zeichensatz der Daten").description
                .put("Die Daten können bspw. deutsche Umlaute enthalten, die nach dem Hochladen falsch dargestellt werden. "
                        + "Mit dem Ändern des Zeichensatzes kann dies korrigiert werden.").extendedUI
                                .put(new PromptUIBuilder() {

                                    private String encoding;

                                    @Override
                                    public void submit(ImporterPrompt prompt) {
                                        formatAndOptions().setEncoding(encoding);
                                        updateOptions();
                                        prompt.ok.set(true);
                                        prompt.value.set(encoding);
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
                                                encoding = encodings.get(c.getSelectionIndex());
                                            }
                                        });
                                        encoding = formatAndOptions().encoding();
                                        int index = encodings.indexOf(encoding);
                                        if (index != -1) {
                                            combo.select(index);
                                        }
                                    }
                                });
        site.newPrompt("headline").summary.put("Kopfzeile").description
                .put("Welche Zeile enhält die Spaltenüberschriften?").extendedUI.put(new PromptUIBuilder() {
                    
                    private int index;

                    @Override
                    public void createContents(ImporterPrompt prompt, Composite parent) {
                        // TODO use a rhei numberfield here
                        Text text = new Text(parent, SWT.RIGHT);
                        text.setText(formatAndOptions().headerLines());
                        text.addModifyListener(event -> {
                            Text t = (Text) event.getSource();
                            // can throw an exception
                            index = Integer.parseInt(t.getText());
                        });
                        // initial value
                        index = Integer.parseInt(text.getText());
                    }

                    @Override
                    public void submit(ImporterPrompt prompt) {
                        formatAndOptions().setHeaderLines(index);
                        updateOptions();
                        prompt.ok.set(true);
                        prompt.value.set(String.valueOf(index));
                    }
                });
    }

    @Override
    protected CSVFormatAndOptions defaultOptions() {
        return CSVFormatAndOptions.createDefault();
    }
}
