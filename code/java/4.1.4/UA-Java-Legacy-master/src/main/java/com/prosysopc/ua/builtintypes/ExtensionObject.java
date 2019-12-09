/* Copyright (c) 1996-2015, OPC Foundation. All rights reserved.
   The source code in this file is covered under a dual-license scenario:
     - RCL: for OPC Foundation members in good-standing
     - GPL V2: everybody else
   RCL license terms accompanied with this source code. See http://opcfoundation.org/License/RCL/1.00/
   GNU General Public License as published by the Free Software Foundation;
   version 2 of the License are accompanied with this source code. See http://opcfoundation.org/License/GPLv2
   This source code is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
*/

package com.prosysopc.ua.builtintypes;

import com.prosysopc.ua.common.NamespaceTable;
import com.prosysopc.ua.core.StatusCodes;
import com.prosysopc.ua.encoding.DecodingException;
import com.prosysopc.ua.encoding.EncodeType;
import com.prosysopc.ua.encoding.EncoderContext;
import com.prosysopc.ua.encoding.EncodingException;
import com.prosysopc.ua.encoding.IEncodeable;
import com.prosysopc.ua.encoding.binary.BinaryDecoder;
import com.prosysopc.ua.encoding.binary.BinaryEncoder;
import com.prosysopc.ua.encoding.binary.IEncodeableSerializer;
import com.prosysopc.ua.encoding.xml.XmlDecoder;
import com.prosysopc.ua.utils.LimitedByteArrayOutputStream;
import com.prosysopc.ua.utils.ObjectUtils;
import com.prosysopc.ua.utils.StackUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Extension object contains a {@link Structure} which is either
 * XML or binary encoded.
 */
public class ExtensionObject {

	private static Logger logger = LoggerFactory.getLogger(ExtensionObject.class);
	
	/**
	 * Create extension object by encoding an encodeable to the defined encoding
	 * using the default serializer.
	 *
	 * @param encodeable the objects to encode
	 * @param encodingType the requested encoding type either QualifiedName.DEFAULT_BINARY_ENCODING or QualifiedName.DEFAULT_XML_ENCODING
	 * @param serializer the serializer to use (default is {@link StackUtils#getDefaultSerializer()})
	 * @return the encodeable as an ExtensionObject
	 * @throws EncodingException if the encodingType is unsupported or the encoding fails
	 * @param ctx a {@link EncoderContext} object.
	 * @deprecated use {@link #encode(Structure, QualifiedName, EncoderContext)}  with the serializer already set in the context.
	 *  This method will take a defensive copy of the given encoder context and set the serializer directly to it.
	 *  If the given serializer is null, the given context is used directly.
	 */
	@Deprecated
	public static ExtensionObject encode(
			Structure encodeable,
			QualifiedName encodingType, IEncodeableSerializer serializer, EncoderContext ctx) throws EncodingException {
		if (encodeable == null)
			return null;
		if (encodingType.equals(QualifiedName.DEFAULT_BINARY_ENCODING))
				return ExtensionObject.binaryEncode(encodeable, serializer, ctx);

		if (encodingType.equals(QualifiedName.DEFAULT_XML_ENCODING))
			return ExtensionObject.xmlEncode(encodeable, serializer);
		throw new EncodingException(StatusCodes.Bad_DataEncodingUnsupported);
	}
	

	/**
	 * Create extension object by encoding an encodeable to the defined encoding
	 * using {@link StackUtils#getDefaultSerializer() the default serializer}.
	 *
	 * @param encodeable the objects to encode
	 * @param encodingType the requested encoding type either QualifiedName.DEFAULT_BINARY_ENCODING or QualifiedName.DEFAULT_XML_ENCODING
	 * @return the encodeable as an ExtensionObject
	 * @throws EncodingException if the encodingType is unsupported or the encoding fails
	 * @param ctx a {@link EncoderContext} object.
	 */
	public static ExtensionObject encode(
			Structure encodeable,
			QualifiedName encodingType, EncoderContext ctx) throws EncodingException {
		return encode(encodeable, encodingType, StackUtils.getDefaultSerializer(), ctx);
	}
	
	/**
	 * Create extension object by encoding an encodeable to a binary format
	 * using the serializer that is set to the given context.
	 *
	 * @param encodeable encodeable
	 * @return binary encoded encodeable
	 * @throws EncodingException on encoding problem
	 * @param ctx a {@link EncoderContext} object.
	 */
	public static ExtensionObject binaryEncode(Structure encodeable, EncoderContext ctx)
	throws EncodingException
	{
		return binaryEncode( encodeable, null, ctx);
	}
	
	
	
	/**
	 * Create extension object by encoding an encodeable to a binary format
	 *
	 * @param encodeable encodeable
	 * @param serializer serializer
	 * @return binary encoded encodeable
	 * @throws EncodingException on encoding problem
	 * @param ctx a {@link EncoderContext} object.
	 * @deprecated use {@link #binaryEncode(Structure, EncoderContext)} with the serializer already set in the context. 
	 *   This method will take a defensive copy of the given encoder context and set the serializer directly to it. 
	 *   If the given serializer is null, the given context is used directly.
	 */
	@Deprecated
	public static ExtensionObject binaryEncode(final Structure encodeable, final IEncodeableSerializer serializer, final EncoderContext ctx)
	throws EncodingException {
		//GH#180, must make defensive copy as we override the serializer
		final EncoderContext context;
		if(serializer == null) {
			context = ctx;
		}else {
			context = ctx.shallowCopy();
			context.setEncodeableSerializer(serializer);
		}

		
		int limit = context.getMaxByteStringLength();
		if(limit == 0) {
			limit = context.getMaxMessageSize();
		}
		if(limit == 0) {
			limit = Integer.MAX_VALUE;
		}
		LimitedByteArrayOutputStream buf = LimitedByteArrayOutputStream.withSizeLimit(limit);
		BinaryEncoder enc = new BinaryEncoder(buf);
		enc.setEncoderContext(context);
		enc.putEncodeable(null, encodeable);
		
		return new ExtensionObject(encodeable.getBinaryEncodeId(), ByteString.valueOf(buf.toByteArray()));
	}
	
	/**
	 * Create extension object by encoding an encodeable to xml format using
	 * the default serializer
	 *
	 * @param encodeable encodeable
	 * @return xml encoded encodeable
	 * @throws EncodingException on encoding problem. Currently always, since the encoding is not supported.
	 */
	public static ExtensionObject xmlEncode(Structure encodeable)
	throws EncodingException
	{
		throw new EncodingException(StatusCodes.Bad_DataEncodingUnsupported);
	}	
	
	/**
	 * Create extension object by encoding an encodeable to xml format
	 *
	 * @param encodeable encodeable
	 * @param serializer serializer
	 * @return xml encoded encodeable
	 * @throws EncodingException on encoding problem. Currently always, since the encoding is not supported.
	 */
	public static ExtensionObject xmlEncode(Structure encodeable, IEncodeableSerializer serializer)
	throws EncodingException
	{
		throw new EncodingException(StatusCodes.Bad_DataEncodingUnsupported);
	}		
	
	final Object object;
	final ExpandedNodeId typeId; // NodeId of a DataType
	final EncodeType encodeType;

	/**
	 * <p>Constructor for ExtensionObject.</p>
	 *
	 * @param typeId a {@link ExpandedNodeId} object.
	 */
	public ExtensionObject(ExpandedNodeId typeId) {
		if (typeId==null)
			throw new IllegalArgumentException("typeId argument must not be null");
		this.typeId = typeId;
		this.object = null;
		this.encodeType = null;
	}
	
	/**
	 * <p>Constructor for ExtensionObject.</p>
	 *
	 * @param typeId a {@link ExpandedNodeId} object.
	 * @param object an array of byte.
	 */
	public ExtensionObject(ExpandedNodeId typeId, ByteString object) {
		if (typeId==null)
			throw new IllegalArgumentException("typeId argument must not be null");
		this.typeId = typeId;
		if (object!=null){
			this.object = object;
			this.encodeType = EncodeType.Binary;
		}else{
			this.object = null;
			this.encodeType = null;
		  //throw new IllegalArgumentException("object argument must not be null");
		}
	}
	
	/**
	 * <p>Constructor for ExtensionObject.</p>
	 *
	 * @param typeId a {@link ExpandedNodeId} object.
	 * @param object an array of byte.
	 * @deprecated use {@link #ExtensionObject(ExpandedNodeId, ByteString)} instead. This method will convert the given byte[] to ByteString.
	 */
	@Deprecated
	public ExtensionObject(ExpandedNodeId typeId, byte[] object) {
		this(typeId, ByteString.valueOf(object));
	}

	/**
	 * <p>Constructor for ExtensionObject.</p>
	 *
	 * @param typeId a {@link ExpandedNodeId} object.
	 * @param object a {@link XmlElement} object.
	 */
	public ExtensionObject(ExpandedNodeId typeId, XmlElement object) {
		if (typeId==null)
			throw new IllegalArgumentException("typeId argument must not be null");
		if (object==null){
			//throw new IllegalArgumentException("object argument must not be null");
			this.object = new XmlElement("");
		} else {
			this.object = object;
		}
		this.typeId = typeId;
//		this.object = object;
		this.encodeType = EncodeType.Xml;
		
//		if (typeId==null)
//			throw new IllegalArgumentException("typeId argument must not be null");
//		if (object==null)
//			throw new IllegalArgumentException("object argument must not be null");
//		this.typeId = typeId;
//		this.object = object;
//		this.encodeType = EncodeType.Xml;
	}
	
	/**
	 * ExtensionObject that encodes the value later when put into a Encoder.
	 * 
	 * @param encodeable a Structure that should later be encoded.
	 */
	public ExtensionObject(Structure encodeable){
	  this.encodeType = null;
	  this.typeId = null;
	  this.object = encodeable;
	}
	
	/**
	 * Returns the type of the raw data returned by {@link #getObject()}. Returns null if this {@link ExtensionObject} already contains a {@link Structure}.
	 */
	public EncodeType getEncodeType() {
		return encodeType;
	}
	
	/**
	 * Get the object within this {@link ExtensionObject}. Can either be an already decoded Structure, raw encoded format as either {@link ByteString} or {@link XmlElement}.
	 */
	public Object getObject() {
		return object;
	}

	/**
	 * <p>Getter for the field <code>typeId</code>.</p>
	 *
	 * @return a {@link ExpandedNodeId} object.
	 */
	public ExpandedNodeId getTypeId() {
		return typeId;
	}
	
	/**
	 * Decode the extension object
	 *
	 * @param <T> type
	 * @param serializer serializer to use
	 * @param ctx context
	 * @param namespaceTable namespace table
	 * @return decoded object
	 * @throws DecodingException if any.
	 */
	@SuppressWarnings("unchecked")
	public <T extends IEncodeable> T decode(IEncodeableSerializer serializer, EncoderContext ctx,
			NamespaceTable namespaceTable)
	throws DecodingException {
		if (object==null)
		{
			Class<? extends IEncodeable> clazz = serializer.getClass(typeId);
			try {
				return (T) clazz.newInstance();
			} catch (InstantiationException e) {
				// Unexpected
				throw new DecodingException(e);
			} catch (IllegalAccessException e) {
				// Unexpected
				throw new DecodingException(e);
			}
		}
		
		//an already decoded value
		if(object instanceof Structure){
		  T r = (T) object;
		  return r;
		}
		
		if (object instanceof XmlElement) {
			Class<? extends IEncodeable> clazz = serializer.getClass(typeId);
			logger.debug("decode: typeId={} class={}", typeId, clazz);
			if (clazz == null)
				throw new DecodingException("No serializer defined for class " + typeId);
			ctx.setEncodeableSerializer(serializer);
			XmlDecoder dec = new XmlDecoder((XmlElement) object, ctx);
			T result;
			try {
				dec.setNamespaceTable(namespaceTable);
				boolean inElement = dec.peek(clazz.getSimpleName());
				if (inElement)
					dec.getStartElement();
				result = (T) serializer.getEncodeable(clazz, dec);
				if (inElement)
					dec.getEndElement();
			} finally {
				dec.close();
			}
			return result;
		}

		if (object instanceof ByteString) {
			Class<? extends IEncodeable> clazz = serializer.getClass(typeId);
			ctx.setEncodeableSerializer(serializer);
			BinaryDecoder dec = new BinaryDecoder(((ByteString)object).getValue());
			dec.setEncoderContext(ctx);
			return (T) serializer.getEncodeable(clazz, dec);
		}

		throw new Error("unexpected");
	}
	
	/**
	 * Attempts to decode the extension object using the default serializer of the stack.
	 *
	 * @param <T> type
	 * @param ctx context
	 * @return decoded object
	 * @throws DecodingException if any.
	 * 
	 */
	public <T extends IEncodeable> T decode(EncoderContext ctx)
	throws DecodingException {
		return decode(ctx, ctx.getNamespaceTable());
	}

	/**
	 * <p>decode.</p>
	 *
	 * @param ctx context
	 * @param namespaceTable a {@link NamespaceTable} object.
	 * @param <T> a T object.
	 * @return a T object.
	 * @throws DecodingException if any.
	 */
	@SuppressWarnings("unchecked")
	public <T extends IEncodeable> T decode(EncoderContext ctx,
			NamespaceTable namespaceTable)
	throws DecodingException {
		return (T) decode(ctx.getEncodeableSerializer(), ctx, namespaceTable);
	}

	@Override
	public int hashCode() {
		if(object == null){
			return 0;
		}
		//Good enough for most use-cases
		return object.hashCode();
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof ExtensionObject))
			return false;
		ExtensionObject other = (ExtensionObject) obj;
		
		//typeId can be null if lazy-encoding
		//this will return true if both are null
		if(!ObjectUtils.equals(typeId, other.typeId)) {
			return false;
		}
		
		return ObjectUtils.equals(object, other.object);
	}

	/**
	 * Returns true, if the {@link #getObject()} is encoded value. Null is considered to be encoded value.
	 * 
	 * @return true if value is encoded
	 */
	public boolean isEncoded(){
		if(object == null){
			return true;
		}
		return !(object instanceof Structure);
	}


	@Override
	public String toString() {
		return "ExtensionObject [typeId=" + typeId + ", encodeType="+ encodeType + ", object=" + object + "]";
	}

	

	
}
