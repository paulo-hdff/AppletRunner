/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pdfbox3.pdmodel.fixup.processor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.logging.Logger;
import java.util.logging.Level;
import org.apache.fontbox3.ttf.TrueTypeFont;
import org.apache.pdfbox3.cos.COSDictionary;
import org.apache.pdfbox3.cos.COSName;
import org.apache.pdfbox3.pdmodel.PDDocument;
import org.apache.pdfbox3.pdmodel.PDPage;
import org.apache.pdfbox3.pdmodel.PDResources;
import org.apache.pdfbox3.pdmodel.font.FontMapper;
import org.apache.pdfbox3.pdmodel.font.FontMappers;
import org.apache.pdfbox3.pdmodel.font.FontMapping;
import org.apache.pdfbox3.pdmodel.font.PDType0Font;
import org.apache.pdfbox3.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox3.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox3.pdmodel.interactive.annotation.PDAppearanceStream;
import org.apache.pdfbox3.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox3.pdmodel.interactive.form.PDField;
import org.apache.pdfbox3.pdmodel.interactive.form.PDFieldFactory;
import org.apache.pdfbox3.pdmodel.interactive.form.PDVariableText;

/**
 *  Generate field entries from page level widget annotations
 *  if there AcroForm /Fields entry is empty.
 * 
 */
public class AcroFormOrphanWidgetsProcessor extends AbstractProcessor
{
    
    private static final Logger LOG = Logger.getLogger(AcroFormOrphanWidgetsProcessor.class.getName());

    public AcroFormOrphanWidgetsProcessor(PDDocument document)
    { 
        super(document); 
    }

    @Override
    public void process()
    {
        /*
         * Get the AcroForm in it's current state.
         *
         * Also note: getAcroForm() applies a default fixup which this processor
         * is part of. So keep the null parameter otherwise this will end
         * in an endless recursive call
         */
        PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm(null);

        if (acroForm != null)
        {            
            resolveFieldsFromWidgets(acroForm);
        } 
    }

    private void resolveFieldsFromWidgets(PDAcroForm acroForm)
    {
        LOG.log(Level.FINER,"rebuilding fields from widgets");

        PDResources resources = acroForm.getDefaultResources();
        if (resources == null)
        {
            // failsafe. Currently resources is never null because defaultfixup is called first.
            LOG.log(Level.FINER,"AcroForm default resources is null");
            return;
        }

        List<PDField> fields = new ArrayList<>();
        Map<String, PDField> nonTerminalFieldsMap = new HashMap<>();
        for (PDPage page : document.getPages())
        {
            try
            {
                handleAnnotations(acroForm, resources, fields, page.getAnnotations(), nonTerminalFieldsMap);
            }
            catch (IOException ioe)
            {
                LOG.log(Level.FINER,"couldn't read annotations for page " + ioe.getMessage());
            }
        }

        acroForm.setFields(fields);

        for (PDField field : acroForm.getFieldTree())
        {
            if (field instanceof PDVariableText)
            {
                ensureFontResources(resources, (PDVariableText) field);
            }
        }
    }

    private void handleAnnotations(PDAcroForm acroForm, PDResources acroFormResources,
            List<PDField> fields, List<PDAnnotation> annotations,
            Map<String, PDField> nonTerminalFieldsMap)
    {
        for (PDAnnotation annot : annotations)
        {
            if (annot instanceof PDAnnotationWidget)
            {
                addFontFromWidget(acroFormResources, annot);

                COSDictionary parent = annot.getCOSObject().getCOSDictionary(COSName.PARENT);
                if (parent != null)
                {
                    PDField resolvedField = resolveNonRootField(acroForm, parent, nonTerminalFieldsMap);
                    if (resolvedField != null)
                    {
                        fields.add(resolvedField);
                    }
                }
                else
                {
                    PDField field = PDFieldFactory.createField(acroForm, annot.getCOSObject(), null);
                    if (field != null)
                    {
                        fields.add(field);
                    }
                }
            }
        }
    }

    /**
     * Add font resources from the widget to the AcroForm to make sure embedded fonts are being used
     * and not added by ensureFontResources potentially using a fallback font.
     * 
     * @param acroFormResources AcroForm default resources, should not be null.
     * @param annotation annotation, should not be null.
     */
    private void addFontFromWidget(PDResources acroFormResources, PDAnnotation annotation)
    {
        PDAppearanceStream normalAppearanceStream = annotation.getNormalAppearanceStream();
        if (normalAppearanceStream == null)
        {
            return;
        }
        PDResources widgetResources = normalAppearanceStream.getResources();
        if (widgetResources == null)
        {
            return;
        }
        widgetResources.getFontNames().forEach(fontName ->
        {
            if (!fontName.getName().startsWith("+"))
            {
                try
                {
                    if (acroFormResources.getFont(fontName) == null)
                    {
                        acroFormResources.put(fontName, widgetResources.getFont(fontName));
                        LOG.log(Level.FINER,"added font resource to AcroForm from widget for font name " + fontName.getName());
                    }
                }
                catch (IOException ioe)
                {
                    LOG.log(Level.FINER,"unable to add font to AcroForm for font name " + fontName.getName());
                }
            }
            else
            {
                LOG.log(Level.FINER,"font resource for widget was a subsetted font - ignored: " + fontName.getName());
            }
        });
    }

    /*
     *  Widgets having a /Parent entry are non root fields. Go up until the root node is found
     *  and handle from there.
     */
    private PDField resolveNonRootField(PDAcroForm acroForm, COSDictionary parent, Map<String, PDField> nonTerminalFieldsMap)
    {
        while (parent.containsKey(COSName.PARENT))
        {
            parent = parent.getCOSDictionary(COSName.PARENT);
            if (parent == null)
            {
                return null;
            }
        }
        
        if (nonTerminalFieldsMap.get(parent.getString(COSName.T)) == null)
        {
            PDField field = PDFieldFactory.createField(acroForm, parent, null);
            if (field != null)
            {
                nonTerminalFieldsMap.put(field.getFullyQualifiedName(), field);
            }
            return field;
        }

        // this should not happen, likely broken PDF
        return null;
    }


    /*
     *  Lookup the font used in the default appearance and if this is 
     *  not available try to find a suitable font and use that.
     *  This may not be the original font but a similar font replacement
     * 
     *  TODO: implement a font lookup similar as discussed in PDFBOX-2661 so that already existing
     *        font resources might be accepatble.
     *        In such case this must be implemented in PDDefaultAppearanceString too!
     */
    private void ensureFontResources(PDResources defaultResources, PDVariableText field)
    {
        String daString = field.getDefaultAppearance();
        if (daString.startsWith("/") && daString.length() > 1)
        {
            COSName fontName = COSName.getPDFName(daString.substring(1, daString.indexOf(" ")));
            try
            {
                if (defaultResources.getFont(fontName) == null)
                {
                    LOG.log(Level.FINER,"trying to add missing font resource for field " + field.getFullyQualifiedName());
                    FontMapper mapper = FontMappers.instance();
                    FontMapping<TrueTypeFont> fontMapping = mapper.getTrueTypeFont(fontName.getName() , null);
                    if (fontMapping != null)
                    {
                        PDType0Font pdFont = PDType0Font.load(document, fontMapping.getFont(), false);
                        LOG.log(Level.FINER,"looked up font for " + fontName.getName() + " - found " + fontMapping.getFont().getName());
                        defaultResources.put(fontName, pdFont);
                    }
                    else
                    {
                        LOG.log(Level.FINER,"no suitable font found for field " + field.getFullyQualifiedName() + " for font name " + fontName.getName());
                    }
                }
            }
            catch (IOException ioe)
            {
                LOG.log(Level.FINER,"unable to handle font resources for field " + field.getFullyQualifiedName() + ": " + ioe.getMessage());
            }
        }
    }
}