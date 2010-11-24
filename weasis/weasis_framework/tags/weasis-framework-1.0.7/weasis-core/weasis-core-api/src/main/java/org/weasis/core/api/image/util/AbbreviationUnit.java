/*******************************************************************************
 * Copyright (c) 2010 Nicolas Roduit.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Nicolas Roduit - initial API and implementation
 ******************************************************************************/
package org.weasis.core.api.image.util;

/**
 * <code>AbbreviationUnit</code> is similar to the Class <code>Unit</code>, except that the method
 * <code>toString()</code> returns the abbreviation of the unit.
 * <p>
 * JMicroVision - Copyright (c) 2002 -2006 Nicolas Roduit
 * 
 * @author Nicolas Roduit
 * @see oorg.weasis.core.api.image.util.Unit
 */

public class AbbreviationUnit {

    private Unit unit;

    /**
     * Create a new instance
     * 
     * @param unit
     *            Unit
     */
    public AbbreviationUnit(Unit unit) {
        this.unit = unit;
    }

    /**
     * Returns the abbreviation of the unit.
     * 
     * @return the abbreviation of the unit
     */
    @Override
    public String toString() {
        return unit.getAbbreviation();
    }

    /**
     * Returns the unit.
     * 
     * @return Unit
     * @see org.weasis.core.api.image.util.Unit
     */
    public Unit getUnit() {
        return unit;
    }

}
