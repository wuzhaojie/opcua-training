/* ========================================================================
 * Copyright (c) 2005-2015 The OPC Foundation, Inc. All rights reserved.
 *
 * OPC Foundation MIT License 1.00
 * 
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 * The complete license agreement can be found here:
 * http://opcfoundation.org/License/MIT/1.00/
 * ======================================================================*/

package com.prosysopc.ua.core;

import com.prosysopc.ua.builtintypes.ExpandedNodeId;
import com.prosysopc.ua.utils.ObjectUtils;
import com.prosysopc.ua.common.NamespaceTable;

import java.util.Arrays;
import com.prosysopc.ua.builtintypes.Variant;


public class UABinaryFileDataType extends DataTypeSchemaHeader {
	
	public static final ExpandedNodeId ID = new ExpandedNodeId(null, NamespaceTable.OPCUA_NAMESPACE, Identifiers.UABinaryFileDataType.getValue());
	public static final ExpandedNodeId BINARY = new ExpandedNodeId(null, NamespaceTable.OPCUA_NAMESPACE, Identifiers.UABinaryFileDataType_Encoding_DefaultBinary.getValue());
	public static final ExpandedNodeId XML = new ExpandedNodeId(null, NamespaceTable.OPCUA_NAMESPACE, Identifiers.UABinaryFileDataType_Encoding_DefaultXml.getValue());
	
    protected String SchemaLocation;
    protected KeyValuePair[] FileHeader;
    protected Variant Body;
    
    public UABinaryFileDataType() {}
    
    public UABinaryFileDataType(String[] Namespaces, StructureDescription[] StructureDataTypes, EnumDescription[] EnumDataTypes, SimpleTypeDescription[] SimpleDataTypes, String SchemaLocation, KeyValuePair[] FileHeader, Variant Body)
    {
        super(Namespaces, StructureDataTypes, EnumDataTypes, SimpleDataTypes);
        this.SchemaLocation = SchemaLocation;
        this.FileHeader = FileHeader;
        this.Body = Body;
    }
    
    public String getSchemaLocation()
    {
        return SchemaLocation;
    }
    
    public void setSchemaLocation(String SchemaLocation)
    {
        this.SchemaLocation = SchemaLocation;
    }
    
    public KeyValuePair[] getFileHeader()
    {
        return FileHeader;
    }
    
    public void setFileHeader(KeyValuePair[] FileHeader)
    {
        this.FileHeader = FileHeader;
    }
    
    public Variant getBody()
    {
        return Body;
    }
    
    public void setBody(Variant Body)
    {
        this.Body = Body;
    }
    
    /**
      * Deep clone
      *
      * @return cloned UABinaryFileDataType
      */
    public UABinaryFileDataType clone()
    {
        UABinaryFileDataType result = (UABinaryFileDataType) super.clone();
        result.Namespaces = Namespaces==null ? null : Namespaces.clone();
        if (StructureDataTypes!=null) {
            result.StructureDataTypes = new StructureDescription[StructureDataTypes.length];
            for (int i=0; i<StructureDataTypes.length; i++)
                result.StructureDataTypes[i] = StructureDataTypes[i].clone();
        }
        if (EnumDataTypes!=null) {
            result.EnumDataTypes = new EnumDescription[EnumDataTypes.length];
            for (int i=0; i<EnumDataTypes.length; i++)
                result.EnumDataTypes[i] = EnumDataTypes[i].clone();
        }
        if (SimpleDataTypes!=null) {
            result.SimpleDataTypes = new SimpleTypeDescription[SimpleDataTypes.length];
            for (int i=0; i<SimpleDataTypes.length; i++)
                result.SimpleDataTypes[i] = SimpleDataTypes[i].clone();
        }
        result.SchemaLocation = SchemaLocation;
        if (FileHeader!=null) {
            result.FileHeader = new KeyValuePair[FileHeader.length];
            for (int i=0; i<FileHeader.length; i++)
                result.FileHeader[i] = FileHeader[i].clone();
        }
        result.Body = Body;
        return result;
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        UABinaryFileDataType other = (UABinaryFileDataType) obj;
        if (Namespaces==null) {
            if (other.Namespaces != null) return false;
        } else if (!Arrays.equals(Namespaces, other.Namespaces)) return false;
        if (StructureDataTypes==null) {
            if (other.StructureDataTypes != null) return false;
        } else if (!Arrays.equals(StructureDataTypes, other.StructureDataTypes)) return false;
        if (EnumDataTypes==null) {
            if (other.EnumDataTypes != null) return false;
        } else if (!Arrays.equals(EnumDataTypes, other.EnumDataTypes)) return false;
        if (SimpleDataTypes==null) {
            if (other.SimpleDataTypes != null) return false;
        } else if (!Arrays.equals(SimpleDataTypes, other.SimpleDataTypes)) return false;
        if (SchemaLocation==null) {
            if (other.SchemaLocation != null) return false;
        } else if (!SchemaLocation.equals(other.SchemaLocation)) return false;
        if (FileHeader==null) {
            if (other.FileHeader != null) return false;
        } else if (!Arrays.equals(FileHeader, other.FileHeader)) return false;
        if (Body==null) {
            if (other.Body != null) return false;
        } else if (!Body.equals(other.Body)) return false;
        return true;
    }
    
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((Namespaces == null) ? 0 : Arrays.hashCode(Namespaces));
        result = prime * result
                + ((StructureDataTypes == null) ? 0 : Arrays.hashCode(StructureDataTypes));
        result = prime * result
                + ((EnumDataTypes == null) ? 0 : Arrays.hashCode(EnumDataTypes));
        result = prime * result
                + ((SimpleDataTypes == null) ? 0 : Arrays.hashCode(SimpleDataTypes));
        result = prime * result
                + ((SchemaLocation == null) ? 0 : SchemaLocation.hashCode());
        result = prime * result
                + ((FileHeader == null) ? 0 : Arrays.hashCode(FileHeader));
        result = prime * result
                + ((Body == null) ? 0 : Body.hashCode());
        return result;
    }
    


	public ExpandedNodeId getTypeId() {
		return ID;
	}

	public ExpandedNodeId getXmlEncodeId() {
		return XML;
	}

	public ExpandedNodeId getBinaryEncodeId() {
		return BINARY;
	}
	
	public String toString() {
		return "UABinaryFileDataType: "+ObjectUtils.printFieldsDeep(this);
	}

}
