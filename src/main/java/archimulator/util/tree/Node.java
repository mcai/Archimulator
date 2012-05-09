package archimulator.util.tree;

import java.util.ArrayList;
import java.util.List;

public class Node<T> {
    private T value;
    private List<Node<T>> children;

    public Node(T value) {
        this.value = value;
        this.children = new ArrayList<Node<T>>();
    }

    public T getValue() {
        return value;
    }

    public List<Node<T>> getChildren() {
        return children;
    }

    public void print() {
        print("", true);
    }

    private void print(String prefix, boolean isTail) {
        System.out.println(prefix + (isTail ? "└── " : "├── ") + value);
        if (children != null) {
            for (int i = 0; i < children.size() - 1; i++) {
                children.get(i).print(prefix + (isTail ? "    " : "│   "), false);
            }
            if (children.size() >= 1) {
                children.get(children.size() - 1).print(prefix + (isTail ? "    " : "│   "), true);
            }
        }
    }

    public static void main(String[] args) {
        Node<Integer> node0 = new Node<Integer>(0);
        Node<Integer> node1 = new Node<Integer>(1);
        Node<Integer> node2 = new Node<Integer>(2);
        Node<Integer> node3 = new Node<Integer>(3);
        Node<Integer> node4 = new Node<Integer>(4);
        node0.getChildren().add(node1);
        node0.getChildren().add(node2);
        node0.getChildren().add(node3);
        node0.getChildren().add(node4);
        node0.print();
    }
}
