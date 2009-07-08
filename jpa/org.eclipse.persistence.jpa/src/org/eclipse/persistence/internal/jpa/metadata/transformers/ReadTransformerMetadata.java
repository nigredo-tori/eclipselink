/*******************************************************************************
 * Copyright (c) 1998, 2009 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0 
 * which accompanies this distribution. 
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at 
 * http://www.eclipse.org/org/documents/edl-v10.php.
 * 
 * Contributors:
 *     Andrei Ilitchev (Oracle), March 7, 2008 
 *        - New file introduced for bug 211300. 
 *     05/16/2008-1.0M8 Guy Pelletier 
 *       - 218084: Implement metadata merging functionality between mapping file   
 ******************************************************************************/  
package org.eclipse.persistence.internal.jpa.metadata.transformers;

import java.lang.annotation.Annotation;

import org.eclipse.persistence.exceptions.ValidationException;
import org.eclipse.persistence.internal.jpa.metadata.ORMetadata;
import org.eclipse.persistence.internal.jpa.metadata.accessors.objects.MetadataAccessibleObject;
import org.eclipse.persistence.mappings.TransformationMapping;

/**
 * INTERNAL:
 * Metadata for ReadTransformer.
 * 
 * @author Andrei Ilitchev
 * @since EclipseLink 1.0 
 */
public class ReadTransformerMetadata extends ORMetadata {
    private Class m_transformerClass;
    
    private String m_transformerClassName;
    private String m_method;

    /**
     * INTERNAL:
     */
    public ReadTransformerMetadata() {
        super("<read-transformer>");
    }
    
    /**
     * INTERNAL:
     */
    protected ReadTransformerMetadata(String xmlElement) {
        super(xmlElement);
    }
    
    /**
     * INTERNAL:
     */
    public ReadTransformerMetadata(Annotation readTransformer, MetadataAccessibleObject accessibleObject) {
        super(readTransformer, accessibleObject);
    
        if (readTransformer != null) {
            m_transformerClass = (Class) MetadataHelper.invokeMethod("transformerClass", readTransformer);
            m_method = (String) MetadataHelper.invokeMethod("method", readTransformer);
        }
    }
    
    /**
     * INTERNAL:
     * Used for OX mapping.
     */
    public String getMethod() {
        return m_method;
    }
    
    /**
     * INTERNAL:
     */
    public Class getTransformerClass() {
        return m_transformerClass;
    }

    /**
     * INTERNAL:
     * Used for OX mapping.
     */
    public String getTransformerClassName() {
        return m_transformerClassName;
    }
    
    /**
     * INTERNAL:
     */
    @Override
    public void initXMLObject(MetadataAccessibleObject accessibleObject) {
        super.initXMLObject(accessibleObject);
    
        m_transformerClass = initXMLClassName(m_transformerClassName);
    }
    
    /**
     * INTERNAL:
     * When this method is called there must be either method or class (but not 
     * both!). If there was not class but className, then by now the class 
     * should have been set.
     */
    public void process(TransformationMapping mapping, String annotatedElementName) {
        if (m_method == null || m_method.equals("")) {
            if (m_transformerClass.equals(void.class)) {
                throw ValidationException.readTransformerHasNeitherClassNorMethod(annotatedElementName);
            } else {
                // We can't use isAssignableFrom here. When static weaving is 
                // used we will have class loader dependencies that will cause 
                // the isAssignableFrom check to always return false.
                if (MetadataHelper.classImplementsInterface(m_transformerClass, "org.eclipse.persistence.mappings.transformers.AttributeTransformer")) {
                    mapping.setAttributeTransformerClassName(m_transformerClass.getName());
                } else {
                    throw ValidationException.readTransformerClassDoesntImplementAttributeTransformer(annotatedElementName);
                }
            }
        } else {
            if (m_transformerClass.equals(void.class)) {
                mapping.setAttributeTransformation(m_method);
            } else {
                throw ValidationException.readTransformerHasBothClassAndMethod(annotatedElementName);
            }
        }
    }

    /**
     * INTERNAL:
     * Used for OX mapping.
     */
    public void setMethod(String method) {
        m_method = method;
    }
    
    /**
     * INTERNAL:
     */
    public void setTransformerClass(Class transformerClass) {
        m_transformerClass = transformerClass;
    }
    
    /**
     * INTERNAL:
     * Used for OX mapping.
     */
    public void setTransformerClassName(String transformerClassName) {
        m_transformerClassName = transformerClassName;
    }
}
