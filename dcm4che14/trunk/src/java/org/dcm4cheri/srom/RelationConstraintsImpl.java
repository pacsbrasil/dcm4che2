/*$Id$*/
/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2001,2002 by TIANI MEDGRAPH AG <gunter.zeilinger@tiani.com>*
 *                                                                           *
 *  This file is part of dcm4che.                                            *
 *                                                                           *
 *  This library is free software; you can redistribute it and/or modify it  *
 *  under the terms of the GNU Lesser General Public License as published    *
 *  by the Free Software Foundation; either version 2 of the License, or     *
 *  (at your option) any later version.                                      *
 *                                                                           *
 *  This library is distributed in the hope that it will be useful, but      *
 *  WITHOUT ANY WARRANTY; without even the implied warranty of               *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        *
 *  Lesser General Public License for more details.                          *
 *                                                                           *
 *  You should have received a copy of the GNU Lesser General Public         *
 *  License along with this library; if not, write to the Free Software      *
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA  *
 *                                                                           *
 *****************************************************************************/

package org.dcm4cheri.srom;

import org.dcm4che.srom.*;
import org.dcm4che.dict.UIDs;
import java.util.*;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0
 */
abstract class RelationConstraintsImpl implements RelationConstraints {
    // Constants -----------------------------------------------------     
    public static final RelationConstraints KEY_OBJECT =
            new RelationConstraints() {
        public void check(Content source, Content.RelationType relation,
                Content target) {
            if (!(source instanceof ContainerContent))
                throw new IllegalArgumentException("" + source);
              
            if (relation == Content.RelationType.CONTAINS) {
                if (!(target instanceof TextContent ||
                        target instanceof CompositeContent))
                    throw new IllegalArgumentException("" + target);
            } else if (relation == Content.RelationType.HAS_OBS_CONTEXT) {
                if (!(target instanceof TextContent ||
                        target instanceof CodeContent ||
                        target instanceof UIDRefContent ||
                        target instanceof PNameContent))
                    throw new IllegalArgumentException("" + target);
            } else if (relation == Content.RelationType.HAS_CONCEPT_MOD) {
                if (!(target instanceof CodeContent))
                    throw new IllegalArgumentException("" + target);
            } else
                throw new IllegalArgumentException("" + relation);
        }
    };
    
    public static final RelationConstraints BASIC_TEXT_SR =
            new RelationConstraintsImpl() {
        void checkTarget(Content target) {
            if (target instanceof ReferencedContent ||
                    target instanceof NumContent ||
                    target instanceof SCoordContent ||
                    target instanceof TCoordContent)
                throw new IllegalArgumentException("" + target);
        }
        void checkSelectedFrom(Content source, Content target) {
            throw new IllegalArgumentException(
                    "" + Content.RelationType.SELECTED_FROM);
        }
                
    };
  
    public static final RelationConstraints ENHANCED_SR =
            new RelationConstraintsImpl() {
        void checkTarget(Content target) {
            if (target instanceof ReferencedContent) {
                throw new IllegalArgumentException("" + target);
            }
        }
    };
    // Attributes ----------------------------------------------------

    // Constructors --------------------------------------------------
    public static RelationConstraints valueOf(String sopClassUID) {
        if (UIDs.BasicTextSR.equals(sopClassUID))
            return BASIC_TEXT_SR;
        if (UIDs.EnhancedSR.equals(sopClassUID))
            return ENHANCED_SR;
        if (UIDs.ComprehensiveSR.equals(sopClassUID))
            return null; // not yet implemented
        if (UIDs.KeyObjectSelectionDocument.equals(sopClassUID))
            return KEY_OBJECT;
        return null;
    }         

    // Methodes ------------------------------------------------------
    static boolean isContainerOrComposite(Content content) {
        return content instanceof ContainerContent
                || content instanceof CompositeContent;
    }
    
    void checkTarget(Content target) {
    }
    
    void checkContains(Content source, Content target) {
        if (!(source instanceof ContainerContent))
            throw new IllegalArgumentException("" + source);
    }

    void checkHasObsContext(Content source, Content target) {
        if (!(source instanceof ContainerContent))
            throw new IllegalArgumentException("" + source);
        if (isContainerOrComposite(target))
            throw new IllegalArgumentException("" + target);
    }

    void checkHasAcqContext(Content source, Content target) {
        if (!isContainerOrComposite(source))
            throw new IllegalArgumentException("" + source);
        if (isContainerOrComposite(target))
            throw new IllegalArgumentException("" + target);
    }

    void checkHasConceptMod(Content source, Content target) {
        if (!(target instanceof TextContent ||
                target instanceof CodeContent))
            throw new IllegalArgumentException("" + target);
    }

    void checkHasProperties(Content source, Content target) {
        if (!(source instanceof TextContent ||
                source instanceof CodeContent ||
                source instanceof NumContent)) {
            throw new IllegalArgumentException("" + source);
        }
        if (target instanceof ContainerContent) {
            throw new IllegalArgumentException("" + target);
        }
    }

    void checkInferredFrom(Content source, Content target) {
        checkHasProperties(source, target);
    }
    
    void checkSelectedFrom(Content source, Content target) {
        if (source instanceof SCoordContent) {
            if (!(target instanceof ImageContent))
                throw new IllegalArgumentException("" + target);
        } else if (source instanceof TCoordContent) {
            if (!(target instanceof SCoordContent ||
                    target instanceof ImageContent ||
                    target instanceof WaveformContent))
                throw new IllegalArgumentException("" + target);
        } else
            throw new IllegalArgumentException("" + source);
    }

    public void check(Content source, Content.RelationType relation,
            Content target) {
        
        checkTarget(target);

        if (relation == Content.RelationType.CONTAINS)
            checkContains(source, target);
        else if (relation == Content.RelationType.HAS_OBS_CONTEXT)
            checkHasObsContext(source, target);
        else if (relation == Content.RelationType.HAS_ACQ_CONTEXT)
            checkHasAcqContext(source, target);
        else if (relation == Content.RelationType.HAS_CONCEPT_MOD)
            checkHasConceptMod(source, target);
        else if (relation == Content.RelationType.HAS_PROPERTIES)
            checkHasProperties(source, target);
        else if (relation == Content.RelationType.INFERRED_FROM)
            checkInferredFrom(source, target);
        else if (relation == Content.RelationType.SELECTED_FROM)
            checkSelectedFrom(source, target);
        else
            throw new IllegalArgumentException("" + relation);
    }                    
}