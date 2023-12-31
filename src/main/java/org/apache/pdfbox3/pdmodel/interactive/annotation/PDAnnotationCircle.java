/*
 * Copyright 2018 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.pdfbox3.pdmodel.interactive.annotation;

import org.apache.pdfbox3.cos.COSDictionary;
import org.apache.pdfbox3.pdmodel.PDDocument;
import org.apache.pdfbox3.pdmodel.interactive.annotation.handlers.PDAppearanceHandler;
import org.apache.pdfbox3.pdmodel.interactive.annotation.handlers.PDCircleAppearanceHandler;

/**
 * @author Paul King
 */
public class PDAnnotationCircle extends PDAnnotationSquareCircle
{
    /**
     * The type of annotation.
     */
    public static final String SUB_TYPE = "Circle";

    private PDAppearanceHandler customAppearanceHandler;

    public PDAnnotationCircle()
    {
        super(SUB_TYPE);
    }

    /**
     * Creates a circle annotation from a COSDictionary, expected to be a correct object definition.
     *
     * @param field the PDF object to represent as a field.
     */
    public PDAnnotationCircle(COSDictionary field)
    {
        super(field);
    }

    /**
     * Set a custom appearance handler for generating the annotations appearance streams.
     * 
     * @param appearanceHandler custom appearance handler
     */
    public void setCustomAppearanceHandler(PDAppearanceHandler appearanceHandler)
    {
        customAppearanceHandler = appearanceHandler;
    }

    @Override
    public void constructAppearances()
    {
        this.constructAppearances(null);
    }

    @Override
    public void constructAppearances(PDDocument document)
    {
        if (customAppearanceHandler == null)
        {
            PDCircleAppearanceHandler appearanceHandler = new PDCircleAppearanceHandler(this, document);
            appearanceHandler.generateAppearanceStreams();
        }
        else
        {
            customAppearanceHandler.generateAppearanceStreams();
        }
    }
}
