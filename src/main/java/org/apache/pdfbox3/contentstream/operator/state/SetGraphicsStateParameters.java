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
package org.apache.pdfbox3.contentstream.operator.state;

import java.io.IOException;
import java.util.List;

import java.util.logging.Logger;
import java.util.logging.Level;
import org.apache.pdfbox3.cos.COSBase;
import org.apache.pdfbox3.cos.COSName;
import org.apache.pdfbox3.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.apache.pdfbox3.contentstream.operator.Operator;
import org.apache.pdfbox3.contentstream.operator.OperatorName;
import org.apache.pdfbox3.contentstream.operator.OperatorProcessor;
import org.apache.pdfbox3.contentstream.PDFStreamEngine;
import org.apache.pdfbox3.contentstream.operator.MissingOperandException;

/**
 * gs: Set parameters from graphics state parameter dictionary.
 *
 * @author Ben Litchfield
 */
public class SetGraphicsStateParameters extends OperatorProcessor
{
    private static final Logger LOG = Logger.getLogger(SetGraphicsStateParameters.class.getName());

    public SetGraphicsStateParameters(PDFStreamEngine context)
    {
        super(context);
    }

    @Override
    public void process(Operator operator, List<COSBase> arguments) throws IOException
    {
        if (arguments.isEmpty())
        {
            throw new MissingOperandException(operator, arguments);
        }
        COSBase base0 = arguments.get(0);
        if (!(base0 instanceof COSName))
        {
            return;
        }
        
        // set parameters from graphics state parameter dictionary
        COSName graphicsName = (COSName) base0;
        PDFStreamEngine context = getContext();
        PDExtendedGraphicsState gs = context.getResources().getExtGState(graphicsName);
        if (gs == null)
        {
            LOG.log(Level.SEVERE,"name for 'gs' operator not found in resources: /" + graphicsName.getName());
            return;
        }
        gs.copyIntoGraphicsState( context.getGraphicsState() );
    }

    @Override
    public String getName()
    {
        return OperatorName.SET_GRAPHICS_STATE_PARAMS;
    }
}
