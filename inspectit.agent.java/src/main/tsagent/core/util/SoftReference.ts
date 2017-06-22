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
    private target: T | null = null;

    constructor(public readonly owner: SoftReferencesPool<T>) { }

    /**
     * Sets the target of this reference.
     * 
     * @param newTarget the new target or null to clear the reference.
     */
    public setTarget(newTarget: T | null) {
        if (this.target) {
            this.owner.activeReferences.remove(this);
        }
        this.target = newTarget;
        if (newTarget) {
            this.owner.activeReferences.addAsHead(this);
            this.owner.ensureBounds();
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

    public newReference(target?: T): SoftReference<T> {
        const result = new SoftReference<T>(this);
        if (target) {
            result.setTarget(target);
        }
        return result;
    }

    public ensureBounds() {
        while (this.activeReferences.length > this.referencesLimit) {
            this.activeReferences.tail!.setTarget(null);
        }
    }
}