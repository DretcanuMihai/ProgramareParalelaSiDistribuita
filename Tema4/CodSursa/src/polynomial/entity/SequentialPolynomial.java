package polynomial.entity;

import polynomial.Monomial;

import java.util.Iterator;

public class SequentialPolynomial implements Polynomial {

    private final Node head;

    public SequentialPolynomial() {
        this.head = new Node(null, null);
    }

    @Override
    public void add(Monomial monomial) {
        //first we find where we need to insert the monomial - we find the position such that
        //current.exponent < monomial.exponent <= current.next.exponent
        //if current is the head of the list, we consider its exponent to be -infinity
        //if current.next is null, we consider it's exponent to be +infinity
        Node current = head;
        while ((current.next != null) && (monomial.getExponent() > current.next.monomial.getExponent())) {
            current = current.next;
        }

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
    }

    @Override
    public Iterator<Monomial> iterator() {
        return new MonomialIterator(head);
    }

    private static class MonomialIterator implements Iterator<Monomial> {

        private Node current;

        public MonomialIterator(Node current) {
            this.current = current;
        }

        @Override
        public boolean hasNext() {
            return current.next != null;
        }

        @Override
        public Monomial next() {
            current = current.next;
            return current.monomial;
        }
    }

    private static class Node {

        private final Monomial monomial;

        private Node next;

        public Node(Monomial monomial, Node next) {
            this.monomial = monomial;
            this.next = next;
        }
    }
}
