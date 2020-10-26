package org.luolun.ConcurrentLinkedQueueDemo;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;

/**
 * 第二个版本，实现延后1次更新head/tail，看看性能如何
 * @param <E> 节点值类型
 */
public class ConcurrentLinkedQueue2<E> implements Queue<E> {

    private static final Unsafe unsafe = UnsafeUtil.createUnsafe();

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
            assert unsafe != null;
            headOffset = unsafe.objectFieldOffset(ConcurrentLinkedQueue2.class.getDeclaredField("head"));
            tailOffset = unsafe.objectFieldOffset(ConcurrentLinkedQueue2.class.getDeclaredField("tail"));
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
        return unsafe.compareAndSwapObject(this, headOffset, tail, newTail);
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
                if (casNext(p, null, newTail)) return true;
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
                // head.next = head.next.next;
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
