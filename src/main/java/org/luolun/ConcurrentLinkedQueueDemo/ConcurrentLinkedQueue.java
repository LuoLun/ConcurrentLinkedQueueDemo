package org.luolun.ConcurrentLinkedQueueDemo;

import sun.misc.Unsafe;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.function.Predicate;

/**
 * 不支持 {@link #remove(Object)}、{@link #removeIf(Predicate)}、{@link #iterator()} 操作的话，就不需要考虑节点为空的情况，
 * 可减少edge case，实现起来比较简单，方便理解和学习；这种实现使得null也可以作为节点内容
 * @param <E> 节点值类型
 */
public class ConcurrentLinkedQueue<E> implements Queue<E> {

    private static Unsafe unsafe = UnsafeUtil.createUnsafe();

    private static class Node<E> {
        volatile E value;
        volatile Node<E> next;

        Node(E value) {
            this.value = value;
        }
    }

    private volatile Node<E> head = new Node<>(null);
    private volatile Node<E> tail = head;

    private static final long headOffset;
    private static final long tailOffset;
    private static final long nodeValueOffset;
    private static final long nodeNextOffset;

    static {
        try {
            headOffset = unsafe.objectFieldOffset(ConcurrentLinkedQueue.class.getDeclaredField("head"));
            tailOffset = unsafe.objectFieldOffset(ConcurrentLinkedQueue.class.getDeclaredField("tail"));
            nodeValueOffset = unsafe.objectFieldOffset(Node.class.getDeclaredField("value"));
            nodeNextOffset = unsafe.objectFieldOffset(Node.class.getDeclaredField("next"));
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean casHead(Node<E> oldHead, Node<E> newHead) {
        return unsafe.compareAndSwapObject(this, headOffset, head, newHead);
    }

    private boolean casTail(Node<E> oldTail, Node<E> newTail) {
        return unsafe.compareAndSwapObject(this, tailOffset, tail, newTail);
    }

    private boolean casValue(Node<E> node, E oldValue, E newValue) {
        if (node == null) throw new RuntimeException("node should not be null!");
        return unsafe.compareAndSwapObject(node, nodeValueOffset, oldValue, newValue);
    }

    private boolean casNext(Node<E> node, Node<E> oldNext, Node<E> newNext) {
        if (node == null) throw new RuntimeException("node should not be null!");
        return unsafe.compareAndSwapObject(node, nodeNextOffset, oldNext, newNext);
    }

    public boolean add(E e) {
        return offer(e);
    }

    public boolean offer(E e) {
        Node<E> p = tail;
        while (true) {
            if (p.next == null) {
                Node<E> newTail = new Node<>(e);
                if (casNext(p, null, newTail)) {
                    casTail(p, newTail);
                    return true;
                }
            } else if (p.next == p) { // 如果当前节点已经不在链表里，则指向自己作为标记
                p = head;
            } else {
                p = p.next;
            }
        }
    }

    public E remove() {
        return poll();
    }

    public E poll() {
        while (true) {
            Node<E> p = head.next;
            if (p == null) return null;
            E res = p.value;
            if (casNext(head, p, p.next)) {
                return res;
            }
        }
    }

    public E element() {
        if (head.next == null) throw new NoSuchElementException();
        return head.next.value;
    }

    public E peek() {
        if (head.next == null) return null;
        return head.next.value;
    }

    public int size() {
        throw new UnsupportedOperationException("先不做");
    }

    public boolean isEmpty() {
        throw new UnsupportedOperationException("先不做");
    }

    public boolean contains(Object o) {
        throw new UnsupportedOperationException("先不做");
    }

    public Iterator<E> iterator() {
        throw new UnsupportedOperationException();
    }

    public Object[] toArray() {
        throw new UnsupportedOperationException("先不做");
    }

    public <T> T[] toArray(T[] a) {
        throw new UnsupportedOperationException("先不做");
    }

    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    public boolean containsAll(Collection<?> c) {
        throw new UnsupportedOperationException("先不做");
    }

    public boolean addAll(Collection<? extends E> c) {
        throw new UnsupportedOperationException("先不做");
    }

    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("先不做");
    }

    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("先不做");
    }

    public void clear() {
        throw new UnsupportedOperationException("先不做");
    }
}
