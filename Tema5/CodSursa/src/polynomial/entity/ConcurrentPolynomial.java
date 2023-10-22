package polynomial.entity;

import polynomial.Monomial;

import java.util.Iterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ConcurrentPolynomial implements Polynomial {

    private final Node head;

    public ConcurrentPolynomial() {
        this.head = new Node(null, null);
    }

    @Override
    public void add(Monomial monomial) {
        //first we find where we need to insert the monomial - we find the position such that
        //current.exponent < monomial.exponent <= current.next.exponent
        //if current is the head of the list, we consider its exponent to be -infinity
        //if current.next is null, we consider its exponent to be +infinity

        Node current = head;
        current.lock.lock();
        if (current.next != null) {
            current.next.lock.lock();
        }

        while ((current.next != null) && (monomial.getExponent() > current.next.monomial.getExponent())) {
            current.lock.unlock();
            current = current.next;
            if (current.next != null) {
                current.next.lock.lock();
            }
        }

        Node oldNext = current.next;
        //if our monomial's exponent doesn't match the next node's exponent (next==null means +infinity),
        //then we need to insert the monomial between current and current.next
        if ((current.next == null) || (monomial.getExponent() != current.next.monomial.getExponent())) {
            current.next = new Node(monomial, current.next);
        } else {
            //else, we need to update the value of the current node
            int newValue = current.next.monomial.getCoefficient() + monomial.getCoefficient();
            // if the new values is non-zero, we just update the value
            if (newValue != 0) {
                current.next.monomial.setCoefficient(newValue);
            } else {
                //else, we "delete" the node
                current.next = current.next.next;
            }
        }
        current.lock.unlock();
        if (oldNext != null) {
            oldNext.lock.unlock();
        }
    }

    @Override
    public Iterator<Monomial> iterator() {
        return new MonomialIterator(head);
    }

    //the iterator should also be synchronized, but I never actually use it inside a context
    //where it needs to be synchronized. Still, for the sake of doing it, I also put the
    //code needed for synchronizing the iterator and commented it
    private static class MonomialIterator implements Iterator<Monomial> {

        private Node current;

        public MonomialIterator(Node current) {
            this.current = current;
//            current.lock.lock();
        }

        @Override
        public boolean hasNext() {
//            boolean hasNext=current.next != null;
//            if(!hasNext){
//                current.lock.unlock();
//            }
//            return hasNext;
            return current.next != null; //this should be commented for the synchronized solution
        }

        @Override
        public Monomial next() {
//            Node oldCurrent = current;
//            current.next.lock.lock();
            current = current.next;
//            oldCurrent.lock.unlock();
            return current.monomial;
        }
    }

    private static class Node {

        private final Monomial monomial;

        private Node next;

        private final Lock lock;

        public Node(Monomial monomial, Node next) {
            this.monomial = monomial;
            this.next = next;
            this.lock = new ReentrantLock();
        }
    }
}
