package edu.wz.sets;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

public class SetsTest {

    /**
     * TreeSet does not support null values, HashSet does. By default TreeSet requires his objects to implement Comparable,
     * and calls the compareTo method. And when null encountered a NullPointerException fired. To add a null value one
     * should use custom comparator that supports null values
     */
    @Test
    public void addNullValueToSet(){
        List<String> data = List.of("Emmmmmmmi", "AnOk", "Coooolio", "Bebebi", "Deeep");
        Set<String> hashSet = new HashSet<>(data);
        TreeSet<String> treeSet = new TreeSet<>(data);
        TreeSet<String> treeSetCustomComparator = new TreeSet<>(Comparator.comparing(s -> s, (s1, s2) -> {
            if(s1 == null && s2 == null)
                return 0;
            if(s1 == null)
                return -1;
            if(s2 == null)
                return 1;
            return s1.compareTo(s2);
        }));

        treeSetCustomComparator.addAll(data);


        hashSet.add(null);
        treeSetCustomComparator.add(null);
        Throwable throwable = catchThrowable( () -> treeSet.add(null));

        assertThat(hashSet).containsNull();
        assertThat(treeSetCustomComparator).containsNull();
        assertThat(throwable.getClass()).isEqualTo(NullPointerException.class);

    }

    /**
     * HashSet does not maintain order, whereas LinkedHasMap does. Both support opportunity to add a null-value in them.
     */
    @Test
    public void demonstratesDifferenceBetweenHashSetVsLinkedHasSet(){
        Set<String> hashSet = new HashSet<>();
        Set<String> linkedHashSet = new LinkedHashSet<>();
        List<String> data = List.of("Emmmmmmmi", "AnOk", "Coooolio", "Bebebi", "Deeep");

        hashSet.addAll(data);
        hashSet.add(null);
        linkedHashSet.addAll(data);
        linkedHashSet.add(null);

        assertThat(linkedHashSet).containsSequence("Emmmmmmmi", "AnOk", "Coooolio", "Bebebi", "Deeep");
        assertThat(hashSet).doesNotContainSequence("Emmmmmmmi", "AnOk", "Coooolio", "Bebebi", "Deeep");
        assertThat(linkedHashSet).containsNull();
        assertThat(hashSet).containsNull();
    }

    /**
     * TreeSet implements NavigableSet and SortedSet and has methods to extract subsets and find greater/lesser elements
     */
    @Test
    public void demonstrateSomeTreeSetOpportunities(){
        List<String> data = List.of("Emmmmmmmi", "AnOk", "Coooolio", "Bebebi", "Deeep");
        TreeSet<String> treeSet = new TreeSet<>(data);

        SortedSet<String> unmodifiableSortedSet = Collections.unmodifiableSortedSet(treeSet);
        Throwable throwable = catchThrowable(() -> unmodifiableSortedSet.add("Muuuueee"));

        assertThat(treeSet.tailSet("Bz")).containsExactly("Coooolio", "Deeep", "Emmmmmmmi");
        assertThat(treeSet.headSet("Ba")).containsExactly("AnOk");
        assertThat(treeSet.higher("Da")).isEqualTo("Deeep");
        assertThat(treeSet.lower("A")).isEqualTo(null);
        assertThat(treeSet.subSet("Ba", "Dz")).containsExactly("Bebebi", "Coooolio", "Deeep");
        assertThat(treeSet.subSet("H", "O")).isNotNull().isEmpty();
        assertThat(throwable.getClass()).isEqualTo(UnsupportedOperationException.class);
    }
}
