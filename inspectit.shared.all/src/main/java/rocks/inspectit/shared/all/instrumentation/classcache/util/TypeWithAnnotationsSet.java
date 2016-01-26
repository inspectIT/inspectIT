package rocks.inspectit.shared.all.instrumentation.classcache.util;

import java.util.Comparator;

import rocks.inspectit.shared.all.instrumentation.classcache.MethodType;
import rocks.inspectit.shared.all.instrumentation.classcache.Type;
import rocks.inspectit.shared.all.instrumentation.classcache.TypeWithAnnotations;

/**
 * Special set for {@link TypeWithAnnotations}. Because both {@link Type} and {@link MethodType} can
 * have annotations, here we need special comparator.
 * 
 * @author Ivan Senic
 * 
 */
public class TypeWithAnnotationsSet extends SortedArraySet<TypeWithAnnotations> {

	/**
	 * {@link TypeWithAnnotations} comparator that defines elements as being equal by the type of
	 * elements and their properties. In fact combines the
	 */
	private static final Comparator<TypeWithAnnotations> TYPE_WITH_ANNOTATIONS_COMPARATOR = new Comparator<TypeWithAnnotations>() {

		public int compare(TypeWithAnnotations o1, TypeWithAnnotations o2) {
			if (o1 instanceof Type && o2 instanceof Type) {
				return TypeSet.FQN_COMPARATOR.compare((Type) o1, (Type) o2);
			} else if (o1 instanceof MethodType && o2 instanceof MethodType) {
				return MethodTypeSet.METHOD_COMPARATOR.compare((MethodType) o1, (MethodType) o2);
			} else {
				// this should never return zero
				return o1.getClass().getName().compareTo(o2.getClass().getName());
			}
		}
	};

	/**
	 * Default constructor. Initializes with {@link #TYPE_WITH_ANNOTATIONS_COMPARATOR}.
	 */
	public TypeWithAnnotationsSet() {
		super(TYPE_WITH_ANNOTATIONS_COMPARATOR);
	}

}
