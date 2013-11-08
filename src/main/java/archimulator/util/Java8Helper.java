package archimulator.util;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Java8Helper {
    public static void main(String[] args) {
        List<String> ls = new ArrayList<>();
        ls.add("al");
        ls.add("fdf");

        System.out.println(ls.stream().filter(l -> l.contains("fdf")).map(String::length).collect(Collectors.toList()));

        ls.stream().forEach(System.out::println);
    }
}
