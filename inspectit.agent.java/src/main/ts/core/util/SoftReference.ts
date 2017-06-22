///<reference path="./DoublyLinkedList.ts"/>

/* tslint:disable:max-classes-per-file*/

/**
 * Soft-Reference Implementation.
 * A soft reference in this implementation belongs to one SoftReferencePool, which is responsible for clearing
 * References when to many are active.
 */
class SoftReference<T> implements IDLListElement<SoftReference<T>> {

    // List pointers
    public next: SoftReference<T>;
    public prev: SoftReference<T>;

    /**
     * Stores the actual referenced element.
     */
    private target: T | null ;

    constructor(public readonly owner: SoftReferencesPool<T>, refTarget?: T) {
        if (refTarget) {
            this.setTarget(refTarget);
        }
     }

    /**
     * Sets the target of this reference.
     * 
     * @param newTarget the new target or null to clear the reference.
     */
    public setTarget(newTarget: T | null) {
        const newlyAdded: boolean = this.target !== null;
        this.target = newTarget;
        if (newTarget) {
            this.owner.notifyReferenceUsed(this, newlyAdded);
        } else if (!newlyAdded) {
            // release if the reference didn't already point to null but now does
            this.owner.notifyReferenceReleased(this);
        }
    }

    public getTarget(): T | null {
        return this.target;
    }
}

/**
 * Pool responsible for managing a set of SoftReferences.
 * This pool ensures that tehre are always less or equal to the given limit references active.
 * References are cleared in LRU order.
 */
class SoftReferencesPool<T> {
    public readonly activeReferences: DoublyLinkedList<SoftReference<T>>;

    constructor(public readonly referencesLimit: number) {
        this.activeReferences = new DoublyLinkedList<SoftReference<T>>();
    }

    /**
     * Internal method, should only be called by SoftReference.
     * 
     * @param reference the reference which was touched.
     * @param newlyAdded true, if this reference was previously not in the lsit of active refernces.
     */
    public notifyReferenceUsed(reference: SoftReference<T>, newlyAdded: boolean) {
        if (!newlyAdded) {
            this.activeReferences.remove(reference);
        }
        this.activeReferences.addAsHead(reference);
        if (newlyAdded) {
            this.ensureBounds();
        }

    }

    /**
     * Internal method, should only be called by SoftReference.
     * 
     * @param reference the reference which was touched.
     */
    public notifyReferenceReleased(reference: SoftReference<T>) {
        this.activeReferences.remove(reference);
    }

    private ensureBounds() {
        while (this.activeReferences.length > this.referencesLimit) {
            this.activeReferences.tail!.setTarget(null);
        }
    }
}