/**
 * Element in a doubly-linked list.
 * Can only participate in one list at a time!
 */
interface IDLListElement<T extends IDLListElement<T>> {
    next: T | null;
    prev: T | null;
}

/**
 * Doubly Linked List implementation.
 * This does not copy given elements, therefore elements can only participate in one list at a time!
 */
class DoublyLinkedList<T extends IDLListElement<T>> {

    public length: number = 0;

    public head: T | null = null;
    public tail: T | null = null;

    public remove(listElement: T) {
        if (this.head === listElement) {
            this.head = listElement.next;
        }
        if (this.tail === listElement) {
            this.tail = listElement.prev;
        }
        if (listElement.next) {
            listElement.next.prev = listElement.prev;
        }
        if (listElement.prev) {
            listElement.prev.next = listElement.next;
        }
        this.length--;
    }

    public addAsHead(listElement: T) {
        if (this.head != null) {
            this.head.prev = listElement;
        }
        if (this.tail == null) {
            this.tail = listElement;
        }
        listElement.next = this.head!;
        listElement.prev = null;
        this.head = listElement;
        this.length++;
    }

}