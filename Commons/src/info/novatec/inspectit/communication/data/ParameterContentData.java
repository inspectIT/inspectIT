package info.novatec.inspectit.communication.data;

import info.novatec.inspectit.cmr.cache.IObjectSizes;
import info.novatec.inspectit.communication.Sizeable;
import info.novatec.inspectit.util.ObjectUtils;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * Stores the content and meta-data of a method parameter or of a field of a class.
 * 
 * @author Patrice Bouillet
 * 
 */
@Entity
public class ParameterContentData implements Serializable, Sizeable, Comparable<ParameterContentData> {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = -8005782295084781051L;

	/**
	 * The id of this instance (if persisted, otherwise <code>null</code>).
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE)
	private long id;

	/**
	 * The name of the parameter. This can only be set if this class stores the content of a class
	 * field as method parameters don't have a name which can be accessed via reflection.
	 */
	private String name;

	/**
	 * The content of the field / parameter.
	 */
	@Column(length = 10000)
	private String content;

	/**
	 * The type of the content (field, return value, parameter).
	 */
	@Enumerated(EnumType.STRING)
	private ParameterContentType contentType;

	/**
	 * If the content of a method parameter is stored the position of the parameter in the signature
	 * has to be saved, too.
	 */
	private int signaturePosition = -1;

	/**
	 * Gets {@link #id}.
	 * 
	 * @return {@link #id}
	 */
	public long getId() {
		return id;
	}

	/**
	 * Sets {@link #id}.
	 * 
	 * @param id
	 *            New value for {@link #id}
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * Gets {@link #name}.
	 * 
	 * @return {@link #name}
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets {@link #name}.
	 * 
	 * @param name
	 *            New value for {@link #name}
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets {@link #content}.
	 * 
	 * @return {@link #content}
	 */
	public String getContent() {
		return content;
	}

	/**
	 * Sets {@link #content}.
	 * 
	 * @param content
	 *            New value for {@link #content}
	 */
	public void setContent(String content) {
		this.content = content;
	}

	/**
	 * Gets {@link #contentType}.
	 * 
	 * @return {@link #contentType}
	 */
	public ParameterContentType getContentType() {
		return contentType;
	}

	/**
	 * Sets {@link #contentType}.
	 * 
	 * @param contentType
	 *            New value for {@link #contentType}
	 */
	public void setContentType(ParameterContentType contentType) {
		this.contentType = contentType;
	}

	/**
	 * Gets {@link #signaturePosition}.
	 * 
	 * @return {@link #signaturePosition}
	 */
	public int getSignaturePosition() {
		return signaturePosition;
	}

	/**
	 * Sets {@link #signaturePosition}.
	 * 
	 * @param signaturePosition
	 *            New value for {@link #signaturePosition}
	 */
	public void setSignaturePosition(int signaturePosition) {
		this.signaturePosition = signaturePosition;
	}

	/**
	 * {@inheritDoc}
	 */
	public long getObjectSize(IObjectSizes objectSizes) {
		return getObjectSize(objectSizes, true);
	}

	/**
	 * {@inheritDoc}
	 */
	public long getObjectSize(IObjectSizes objectSizes, boolean doAlign) {
		long size = objectSizes.getSizeOfObjectHeader();
		size += objectSizes.getPrimitiveTypesSize(3, 0, 1, 0, 1, 0);
		size += objectSizes.getSizeOf(content);
		size += objectSizes.getSizeOf(name);
		size += objectSizes.getSizeOf(contentType);

		if (doAlign) {
			return objectSizes.alignTo8Bytes(size);
		} else {
			return size;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public int compareTo(ParameterContentData other) {
		return ObjectUtils.compare(name, other.name);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((content == null) ? 0 : content.hashCode());
		result = prime * result + ((contentType == null) ? 0 : contentType.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + signaturePosition;
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ParameterContentData other = (ParameterContentData) obj;
		if (content == null) {
			if (other.content != null) {
				return false;
			}
		} else if (!content.equals(other.content)) {
			return false;
		}
		if (contentType == null) {
			if (other.contentType != null) {
				return false;
			}
		} else if (!contentType.equals(other.contentType)) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (signaturePosition != other.signaturePosition) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		return content;
	}

}
