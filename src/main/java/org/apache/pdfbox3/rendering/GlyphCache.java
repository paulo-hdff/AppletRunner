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
package org.apache.pdfbox3.rendering;

import java.awt.geom.GeneralPath;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.apache.pdfbox3.pdmodel.font.PDFontLike;
import org.apache.pdfbox3.pdmodel.font.PDSimpleFont;
import org.apache.pdfbox3.pdmodel.font.PDType0Font;
import org.apache.pdfbox3.pdmodel.font.PDVectorFont;

/**
 * A simple glyph outline cache.
 *
 * @author John Hewson
 */
final class GlyphCache
{
    private static final Logger LOG = Logger.getLogger(GlyphCache.class.getName());
    
    private final PDVectorFont font;
    private final Map<Integer, GeneralPath> cache = new HashMap<>();

    GlyphCache(PDVectorFont font)
    {
        this.font = font;
    }
    
    public GeneralPath getPathForCharacterCode(int code)
    {
        GeneralPath path = cache.get(code);
        if (path != null)
        {
            return path;
        }

        try
        {
            if (!font.hasGlyph(code))
            {
                String fontName = ((PDFontLike) font).getName();
                if (font instanceof PDType0Font)
                {
                    int cid = ((PDType0Font) font).codeToCID(code);
                    String cidHex = String.format("%04x", cid);
                    LOG.log(Level.WARNING,"No glyph for code " + code + " (CID " + cidHex + ") in font " + fontName);
                }
                else if (font instanceof PDSimpleFont)
                {
                    PDSimpleFont simpleFont = (PDSimpleFont) font;
                    LOG.log(Level.WARNING,"No glyph for code " + code + " in " + font.getClass().getSimpleName()
                            + " " + fontName + " (embedded or system font used: "
                            + simpleFont.getFontBoxFont().getName() + ")");
                    if (code == 10 && simpleFont.isStandard14())
                    {
                        // PDFBOX-4001 return empty path for line feed on std14
                        path = new GeneralPath();
                        cache.put(code, path);
                        return path;
                    }
                }
                else
                {
                    LOG.log(Level.WARNING,"No glyph for code " + code + " in font " + fontName);
                }
            }

            path = font.getNormalizedPath(code);
            cache.put(code, path);
            return path;
        }
        catch (IOException e)
        {
            // todo: escalate this error?
            String fontName = ((PDFontLike) font).getName();
            LOG.log(Level.SEVERE,"Glyph rendering failed for code " + code + " in font " + fontName, e);
            return new GeneralPath();
        }
    }
}
