/*******************************************************************************
* Copyright (c) 2011 Oracle. All rights reserved.
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0
* which accompanies this distribution.
* The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
* and the Eclipse Distribution License is available at
* http://www.eclipse.org/org/documents/edl-v10.php.
*
* Contributors:
*     mmacivor - Initial implementation
******************************************************************************/
package org.eclipse.persistence.testing.jaxb.json.xmlvalue;

import javax.xml.bind.annotation.XmlValue;

public class PhoneNumber {

    @XmlValue
    public String number;
    
    public boolean equals(Object obj) {
        if(!(obj instanceof PhoneNumber)) {
            return false;
        } else {
            return this.number.equals(((PhoneNumber)obj).number);
        }
    }
}
