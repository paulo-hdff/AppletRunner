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
package org.apache.pdfbox3.contentstream.operator.graphics;

import java.awt.geom.Path2D;
import java.io.IOException;
import java.util.List;

import org.apache.pdfbox3.cos.COSBase;
import org.apache.pdfbox3.contentstream.PDFGraphicsStreamEngine;
import org.apache.pdfbox3.contentstream.operator.Operator;
import org.apache.pdfbox3.contentstream.operator.OperatorName;

/**
 * f* Fill path using even odd rule.
 *
 * @author Ben Litchfield
 */
public final class FillEvenOddRule extends GraphicsOperatorProcessor
{
    public FillEvenOddRule(PDFGraphicsStreamEngine context)
    {
        super(context);
    }

    @Override
    public void process(Operator operator, List<COSBase> operands) throws IOException
    {
        getGraphicsContext().fillPath(Path2D.WIND_EVEN_ODD);
    }

    @Override
    public String getName()
    {
        return OperatorName.FILL_EVEN_ODD;
    }
}
